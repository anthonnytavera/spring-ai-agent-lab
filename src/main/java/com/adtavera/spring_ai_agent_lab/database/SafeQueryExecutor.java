package com.adtavera.spring_ai_agent_lab.database;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

/**
 * Ejecuta consultas que provienen de un modelo de lenguaje, es decir, de una fuente que no es de confianza.
 *
 * <p>Esta clase concentra <b>toda la defensa</b> del acceso a datos y no sabe nada de IA ni de herramientas:
 * recibe un SQL y lo ejecuta de forma segura. Así la seguridad vive en un solo sitio y se prueba por separado
 * (ver {@code SafeQueryExecutorTest}); las {@code @Tool} solo la usan.
 *
 * <p>Capas de defensa (de la más fuerte a la más débil):
 * <ol>
 *   <li><b>Usuario de base de datos de solo lectura</b> ({@code agent_ro}, creado en {@code schema.sql}).
 *       Es la defensa real: aunque llegue SQL malicioso, la base de datos lo rechaza.</li>
 *   <li><b>Límites</b>: máximo de filas y tiempo máximo por consulta.</li>
 *   <li><b>Filtro de texto</b>: rechaza lo que no parezca un único SELECT. Es una segunda capa útil, pero
 *       por sí sola sería insuficiente (es fácil de eludir).</li>
 * </ol>
 */
@Component
public class SafeQueryExecutor {

	private static final Logger log = LoggerFactory.getLogger(SafeQueryExecutor.class);

	/** Máximo de filas devueltas: acota el tamaño del resultado y el coste en tokens al enviarlo al modelo. */
	static final int MAX_ROWS = 50;

	/** Tiempo máximo por consulta: evita que un SQL costoso bloquee la aplicación. */
	private static final int QUERY_TIMEOUT_SECONDS = 5;

	/** Palabras que indican una sentencia que no es de lectura. Segunda capa de defensa, no la principal. */
	private static final Pattern FORBIDDEN = Pattern.compile(
			"\\b(insert|update|delete|drop|alter|create|truncate|merge|grant|revoke|call|runscript)\\b",
			Pattern.CASE_INSENSITIVE);

	private final JdbcTemplate jdbc;

	/**
	 * Crea una conexión propia con el usuario de solo lectura, distinta del {@link DataSource} de la aplicación.
	 * Así, el agente nunca tiene más permisos que los que se le concedieron en la base de datos.
	 *
	 * @param url      URL JDBC (ver {@code agent.datasource.url})
	 * @param username usuario de solo lectura
	 * @param password contraseña del usuario de solo lectura
	 */
	public SafeQueryExecutor(@Value("${agent.datasource.url}") String url,
			@Value("${agent.datasource.username}") String username,
			@Value("${agent.datasource.password}") String password) {
		DataSource readOnly = new DriverManagerDataSource(url, username, password);
		this.jdbc = new JdbcTemplate(readOnly);
		this.jdbc.setMaxRows(MAX_ROWS);
		this.jdbc.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
	}

	/**
	 * Lista las columnas de las tablas del esquema público.
	 *
	 * @return una fila por columna: {@code TABLE_NAME}, {@code COLUMN_NAME} y {@code DATA_TYPE}
	 */
	public List<Map<String, Object>> listColumns() {
		return jdbc.queryForList("""
				SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE
				FROM INFORMATION_SCHEMA.COLUMNS
				WHERE TABLE_SCHEMA = 'PUBLIC'
				ORDER BY TABLE_NAME, ORDINAL_POSITION
				""");
	}

	/**
	 * Ejecuta una consulta de solo lectura.
	 *
	 * @param sql sentencia que debería ser un único SELECT
	 * @return las filas (cada una, un mapa columna-valor), con un máximo de {@value #MAX_ROWS}
	 * @throws QueryRejectedException si el filtro de texto la rechaza
	 * @throws org.springframework.dao.DataAccessException si la base de datos la rechaza o falla
	 *                                                     (SQL inválido, falta de permisos, tiempo agotado...)
	 */
	public List<Map<String, Object>> executeSelect(String sql) {
		log.info("SQL generado por el modelo: {}", sql);
		String cleaned = sql.trim().replaceAll(";+\\s*$", "");
		if (!cleaned.toLowerCase().startsWith("select") || cleaned.contains(";") || FORBIDDEN.matcher(cleaned).find()) {
			throw new QueryRejectedException("solo se permite un único SELECT de solo lectura.");
		}
		return jdbc.queryForList(cleaned);
	}

}
