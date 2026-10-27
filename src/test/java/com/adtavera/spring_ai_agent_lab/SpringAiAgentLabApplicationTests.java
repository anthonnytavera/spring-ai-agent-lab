package com.adtavera.spring_ai_agent_lab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Comprueba que el contexto de Spring arranca completo (base de datos, tools, ChatClient, Swagger). */
@SpringBootTest(properties = "spring.ai.openai.api-key=test-key")
class SpringAiAgentLabApplicationTests {

	@Test
	void contextLoads() {
	}

}
