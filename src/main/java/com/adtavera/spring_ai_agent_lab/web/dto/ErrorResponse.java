package com.adtavera.spring_ai_agent_lab.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Formato único de error de la API.
 *
 * @param error   categoría corta del error
 * @param message explicación pensada para quien consume la API
 */
@Schema(description = "Error devuelto por la API")
public record ErrorResponse(
		@Schema(description = "Categoría del error", example = "Petición inválida") String error,
		@Schema(description = "Explicación del error", example = "El campo 'question' es obligatorio.") String message) {
}
