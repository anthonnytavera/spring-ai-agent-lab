package com.adtavera.spring_ai_agent_lab.agent;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * El agente. Reúne las cuatro piezas que convierten un modelo de lenguaje en un agente:
 *
 * <ol>
 *   <li><b>Modelo</b>: el {@link ChatClient} encapsula al proveedor configurado en {@code application.properties}.</li>
 *   <li><b>Instrucciones</b> ({@link #SYSTEM_PROMPT}): definen su rol y sus reglas de trabajo.</li>
 *   <li><b>Herramientas</b> ({@link DatabaseTools}): lo que el modelo puede hacer además de escribir texto.</li>
 *   <li><b>Memoria</b> ({@link MessageChatMemoryAdvisor}): el historial de cada conversación.</li>
 * </ol>
 *
 * <p><b>El bucle del agente.</b> Esta clase no programa el bucle; lo ejecuta Spring AI dentro de
 * {@code call()}:
 * <pre>
 *   pregunta ──► modelo ──► ¿pide una herramienta?
 *                              │ sí: la aplicación la ejecuta, devuelve el resultado al modelo y se repite
 *                              └ no: el modelo redacta la respuesta final
 * </pre>
 * El modelo nunca ejecuta nada por sí mismo: solo <i>pide</i> que la aplicación ejecute una herramienta.
 * Por eso el control (y la seguridad) siempre están del lado de la aplicación.
 */
@Service
public class DatabaseAgent {

	/**
	 * Instrucciones de sistema: se envían en cada llamada y fijan el comportamiento del agente.
	 * Es la forma principal de "programar" a un agente; conviene que sean concretas y verificables.
	 */
	private static final String SYSTEM_PROMPT = """
			Eres un asistente que responde preguntas sobre una base de datos H2 de ventas.
			Reglas:
			- Antes de escribir SQL, llama a describeSchema si no conoces las tablas.
			- Usa runSelectQuery para obtener datos; nunca inventes cifras.
			- Si el SQL falla, corrígelo y reintenta.
			- Usa el historial de la conversación para entender preguntas de seguimiento.
			- Responde en español, de forma breve, basándote solo en los resultados obtenidos.
			""";

	private final ChatClient chatClient;

	/**
	 * Nombre de la cabecera HTTP con la que se envía el id de conversación al proveedor del modelo.
	 * Algunos proveedores la exigen para enrutar las peticiones (por ejemplo, {@code x-opencode-session}).
	 * Vacío = no se envía ninguna cabecera.
	 */
	private final String sessionHeader;

	/**
	 * @param builder       lo crea Spring AI ya configurado con el proveedor y el modelo de {@code application.properties}
	 * @param tools         herramientas que el modelo puede pedir ejecutar
	 * @param chatMemory    almacén del historial (en memoria por defecto, creado por Spring AI)
	 * @param sessionHeader cabecera de sesión opcional, ver {@code agent.session-header}
	 */
	public DatabaseAgent(ChatClient.Builder builder, DatabaseTools tools, ChatMemory chatMemory,
			@Value("${agent.session-header:}") String sessionHeader) {
		this.sessionHeader = sessionHeader;
		this.chatClient = builder
				.defaultSystem(SYSTEM_PROMPT)
				.defaultTools(tools)
				// Antes de cada llamada inyecta el historial de la conversación y,
				// al terminar, guarda la nueva pregunta y su respuesta.
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
	}

	/**
	 * Responde una pregunta dentro de una conversación.
	 *
	 * @param conversationId clave del historial: el mismo id comparte memoria, otro id empieza de cero
	 * @param question       pregunta del usuario
	 * @return respuesta final del modelo, redactada a partir de los datos que obtuvo con las herramientas
	 * @throws AgentUnavailableException si el proveedor del modelo no pudo responder
	 */
	public String ask(String conversationId, String question) {
		ChatClient.ChatClientRequestSpec request = chatClient.prompt()
				.user(question)
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));

		// Dependiente del proveedor: OpenAiChatOptions solo existe con el starter de OpenAI.
		// Si se cambia de proveedor, se elimina este bloque y se deja vacía la propiedad agent.session-header.
		if (!sessionHeader.isBlank()) {
			request = request.options(OpenAiChatOptions.builder().customHeaders(Map.of(sessionHeader, conversationId)));
		}

		try {
			return request.call().content();
		}
		catch (RuntimeException e) {
			// El error original (que depende del proveedor) queda como causa y se registra en la capa web.
			throw new AgentUnavailableException("No se obtuvo respuesta del proveedor del modelo", e);
		}
	}

}
