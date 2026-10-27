package com.adtavera.spring_ai_agent_lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>Mapa del proyecto:
 * <ul>
 *   <li>{@code web}: capa HTTP. {@code AgentController} expone {@code POST /ask} y
 *       {@code ApiExceptionHandler} traduce los errores a respuestas HTTP.</li>
 *   <li>{@code agent}: el agente. {@code DatabaseAgent} reúne modelo, instrucciones, herramientas y memoria;
 *       {@code DatabaseTools} son las herramientas que el modelo puede pedir ejecutar.</li>
 *   <li>{@code database}: {@code SafeQueryExecutor} ejecuta el SQL del modelo con toda la defensa
 *       (usuario de solo lectura, límites y filtro).</li>
 *   <li>{@code config}: metadatos de Swagger.</li>
 * </ul>
 * La teoría y la guía de uso están en el README del proyecto.
 */
@SpringBootApplication
public class SpringAiAgentLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiAgentLabApplication.class, args);
	}

}
