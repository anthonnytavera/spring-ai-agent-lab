package com.adtavera.spring_ai_agent_lab.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo de la petición de {@code POST /ask}.
 *
 * @param conversationId identifica la conversación; cada id tiene su propio historial en memoria
 * @param question       pregunta en lenguaje natural
 */
@Schema(description = "Pregunta al agente")
public record AskRequest(
		@Schema(description = "Identificador de la conversación. Con el mismo id el agente recuerda los mensajes "
				+ "anteriores; con otro id empieza de cero. Opcional: si se omite se usa \"default\".",
				example = "ana", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String conversationId,
		@Schema(description = "Pregunta en lenguaje natural sobre clientes, productos y pedidos.",
				example = "¿Cuántos clientes hay en Peru?", requiredMode = Schema.RequiredMode.REQUIRED) String question) {
}
