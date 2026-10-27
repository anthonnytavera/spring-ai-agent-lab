package com.adtavera.spring_ai_agent_lab.database;

/**
 * La consulta fue rechazada por el filtro de {@link SafeQueryExecutor} antes de llegar a la base de datos
 * (no es un único SELECT, o contiene palabras de escritura).
 *
 * <p>Es una excepción propia para poder distinguirla de un error SQL real: el rechazo lo decide la
 * aplicación; el error SQL lo decide la base de datos.
 */
public class QueryRejectedException extends RuntimeException {

	public QueryRejectedException(String message) {
		super(message);
	}

}
