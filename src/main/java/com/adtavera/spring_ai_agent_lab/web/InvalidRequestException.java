package com.adtavera.spring_ai_agent_lab.web;

/**
 * La petición del cliente es incorrecta (por ejemplo, falta la pregunta). Se traduce a HTTP 400.
 *
 * <p>Es una excepción propia, y no {@link IllegalArgumentException}, para que el manejador de errores no
 * convierta en un 400 fallos internos que también usan esa excepción estándar.
 */
public class InvalidRequestException extends RuntimeException {

	public InvalidRequestException(String message) {
		super(message);
	}

}
