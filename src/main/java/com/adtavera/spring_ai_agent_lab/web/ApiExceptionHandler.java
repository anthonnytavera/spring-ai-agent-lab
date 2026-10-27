package com.adtavera.spring_ai_agent_lab.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.adtavera.spring_ai_agent_lab.agent.AgentUnavailableException;
import com.adtavera.spring_ai_agent_lab.web.dto.ErrorResponse;

/**
 * Traduce las excepciones a respuestas HTTP con un formato único ({@link ErrorResponse}).
 *
 * <p>Sin esta clase, cualquier fallo saldría como un 500 genérico de Spring, sin distinguir entre un error
 * del cliente (400) y un fallo del proveedor del modelo (502).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	/** Petición mal formada: falta un campo obligatorio. */
	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Petición inválida", e.getMessage()));
	}

	/** El cuerpo no es un JSON válido (o no se puede leer, por ejemplo por la codificación). */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse("Petición inválida", "El cuerpo debe ser un JSON válido en UTF-8."));
	}

	/**
	 * El proveedor del modelo no respondió. Es un 502 (Bad Gateway): la aplicación funciona, falla el servicio
	 * del que depende. El detalle (clave inválida, saldo, saturación...) se registra en el log y no se envía al
	 * cliente, porque puede contener información interna del proveedor.
	 */
	@ExceptionHandler(AgentUnavailableException.class)
	public ResponseEntity<ErrorResponse> handleAgentUnavailable(AgentUnavailableException e) {
		log.error("Fallo al llamar al proveedor del modelo", e.getCause());
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse("Proveedor del modelo no disponible",
				"No se pudo obtener respuesta del modelo. Revisa los logs de la aplicación para ver el detalle."));
	}

}
