package com.adtavera.spring_ai_agent_lab.agent;

/**
 * El agente no pudo obtener una respuesta del proveedor del modelo (clave inválida, saldo insuficiente,
 * servicio saturado, red caída...).
 *
 * <p>Envuelve el error original, que depende del proveedor y del cliente HTTP, para que el resto de la
 * aplicación (por ejemplo, el manejo de errores de la capa web) no tenga que conocer ninguno de ellos.
 */
public class AgentUnavailableException extends RuntimeException {

	public AgentUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
