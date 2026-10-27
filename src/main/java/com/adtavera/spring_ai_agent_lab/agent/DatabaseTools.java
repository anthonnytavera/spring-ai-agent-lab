package com.adtavera.spring_ai_agent_lab.agent;

import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.adtavera.spring_ai_agent_lab.database.QueryRejectedException;
import com.adtavera.spring_ai_agent_lab.database.SafeQueryExecutor;

/**
 * Herramientas (<i>tools</i>) que el modelo puede pedir ejecutar.
 *
 * <p><b>Cómo funciona el tool calling.</b> Spring AI describe al modelo cada método anotado con
 * {@link Tool}: su nombre, su {@code description} y sus parámetros (los {@link ToolParam}). El modelo no
 * ve el código; solo lee esas descripciones y decide cuándo le conviene usar cada herramienta. Cuando
 * decide usar una, responde con una petición estructurada ("llama a runSelectQuery con este SQL"),
 * Spring AI ejecuta el método Java, convierte el resultado a JSON y se lo devuelve al modelo.
 *
 * <p>Por eso la {@code description} es tan importante como el código: es la "documentación" que el
 * modelo usa para decidir.
 *
 * <p><b>Reparto de responsabilidades.</b> Esta clase solo habla el idioma del modelo: describe las
 * herramientas y convierte rechazos y errores en texto que el modelo pueda leer. La seguridad del acceso
 * a datos no está aquí sino en {@link SafeQueryExecutor}.
 */
@Component
public class DatabaseTools {

	private final SafeQueryExecutor queries;

	public DatabaseTools(SafeQueryExecutor queries) {
		this.queries = queries;
	}

	/**
	 * Herramienta 1: darle al modelo el mapa de la base de datos.
	 *
	 * <p>Sin el esquema, el modelo adivinaría los nombres de tablas y columnas (y se equivocaría).
	 * Dárselo como herramienta, en lugar de escribirlo en el prompt, permite que el esquema cambie
	 * sin tocar las instrucciones y que el modelo lo consulte solo cuando lo necesita.
	 *
	 * @return una fila por columna: nombre de tabla, nombre de columna y tipo de dato
	 */
	@Tool(description = "Devuelve las tablas de la base de datos con sus columnas y tipos. Llámala primero para conocer el esquema antes de escribir SQL.")
	public List<Map<String, Object>> describeSchema() {
		return queries.listColumns();
	}

	/**
	 * Herramienta 2: ejecutar la consulta que el modelo escribió (patrón <i>text-to-SQL</i>).
	 *
	 * <p>Los rechazos y errores no se lanzan como excepción: se devuelven como texto. Así el modelo los lee,
	 * entiende qué falló y corrige su SQL en el siguiente paso del bucle, en lugar de romper toda la petición.
	 *
	 * @param sql sentencia SELECT generada por el modelo
	 * @return las filas (lista de mapas columna-valor) o un mensaje de rechazo/error que el modelo puede leer
	 */
	@Tool(description = "Ejecuta una consulta SQL de solo lectura (un único SELECT, dialecto H2) y devuelve las filas. Máximo 50 filas.")
	public Object runSelectQuery(@ToolParam(description = "Sentencia SELECT en SQL H2") String sql) {
		try {
			return queries.executeSelect(sql);
		}
		catch (QueryRejectedException e) {
			return "Consulta rechazada: " + e.getMessage();
		}
		catch (DataAccessException e) {
			return "Error SQL: " + e.getMessage();
		}
	}

}
