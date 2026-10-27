package com.adtavera.spring_ai_agent_lab.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo de la respuesta de {@code POST /ask}.
 *
 * @param answer respuesta redactada por el modelo a partir de los datos obtenidos
 */
@Schema(description = "Respuesta del agente")
public record AskResponse(
		@Schema(description = "Respuesta en lenguaje natural, basada en los resultados de la base de datos.",
				example = "Hay 2 clientes en Peru: Ana Torres y Maria Lopez.") String answer) {
}
