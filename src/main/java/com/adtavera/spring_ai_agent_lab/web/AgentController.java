package com.adtavera.spring_ai_agent_lab.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.adtavera.spring_ai_agent_lab.agent.DatabaseAgent;
import com.adtavera.spring_ai_agent_lab.web.dto.AskRequest;
import com.adtavera.spring_ai_agent_lab.web.dto.AskResponse;
import com.adtavera.spring_ai_agent_lab.web.dto.ErrorResponse;

/**
 * Capa HTTP del agente: expone un único endpoint, {@code POST /ask}.
 *
 * <p>No contiene lógica de IA. Su única responsabilidad es validar la petición, traducir HTTP a una llamada
 * al {@link DatabaseAgent} y devolver el resultado. Así, la lógica del agente se puede reutilizar desde otro
 * canal (consola, mensajería, tests) sin depender de HTTP. Los errores se traducen en
 * {@link ApiExceptionHandler}.
 */
@RestController
@Tag(name = "Agente", description = "Consultas en lenguaje natural sobre la base de datos de ventas")
public class AgentController {

	/** Conversación que se usa cuando el cliente no envía {@code conversationId}. */
	private static final String DEFAULT_CONVERSATION = "default";

	private final DatabaseAgent agent;

	public AgentController(DatabaseAgent agent) {
		this.agent = agent;
	}

	@Operation(summary = "Hacer una pregunta al agente",
			description = """
					Envía una pregunta en lenguaje natural. El agente consulta el esquema, genera un SELECT, \
					lo ejecuta con un usuario de solo lectura y redacta la respuesta.

					Para mantener una conversación (preguntas de seguimiento como "¿y cuál de ellos gastó más?") \
					reutiliza el mismo `conversationId`.""")
	@ApiResponse(responseCode = "200", description = "Respuesta generada por el agente")
	@ApiResponse(responseCode = "400", description = "Petición inválida: falta `question` o el cuerpo no es un JSON válido",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "502",
			description = "El proveedor del modelo no respondió (clave inválida, saldo insuficiente, servicio saturado...). "
					+ "El detalle está en los logs de la aplicación.",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/ask")
	public AskResponse ask(@RequestBody AskRequest request) {
		if (request.question() == null || request.question().isBlank()) {
			throw new InvalidRequestException("El campo 'question' es obligatorio.");
		}
		String conversationId = request.conversationId() == null || request.conversationId().isBlank()
				? DEFAULT_CONVERSATION
				: request.conversationId();
		return new AskResponse(agent.ask(conversationId, request.question()));
	}

}
