package com.adtavera.spring_ai_agent_lab.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentación OpenAPI.
 *
 * <p>springdoc genera la especificación a partir de los controladores. Con la aplicación en marcha:
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8080/swagger-ui.html}</li>
 *   <li>Especificación JSON: {@code http://localhost:8080/v3/api-docs}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI agentOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Spring AI Agent Lab")
				.version("0.0.1")
				.description("""
						Agente de IA que responde preguntas en lenguaje natural consultando una base de datos H2. \
						Usa *tool calling*: el modelo decide qué herramientas invocar (ver el esquema, ejecutar un SELECT) \
						y redacta la respuesta con los datos reales. Más detalle en el README del proyecto."""));
	}

}
