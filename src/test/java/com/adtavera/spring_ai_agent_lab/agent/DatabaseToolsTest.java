package com.adtavera.spring_ai_agent_lab.agent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Comprueba lo que ve el modelo: las herramientas deben devolver texto legible ante rechazos y errores,
 * en lugar de lanzar excepciones que romperían el bucle del agente.
 */
@SpringBootTest(properties = "spring.ai.openai.api-key=test-key")
class DatabaseToolsTest {

	@Autowired
	DatabaseTools tools;

	@Test
	void describeSchemaReturnsColumns() {
		assertThat(tools.describeSchema()).isNotEmpty();
	}

	@Test
	void validSelectReturnsRows() {
		assertThat(tools.runSelectQuery("SELECT name FROM customers")).isInstanceOf(List.class);
	}

	@Test
	void rejectedQueryIsReturnedAsReadableText() {
		assertThat(tools.runSelectQuery("DROP TABLE customers")).asString().startsWith("Consulta rechazada");
	}

	@Test
	void sqlErrorIsReturnedAsReadableTextSoTheModelCanFixIt() {
		assertThat(tools.runSelectQuery("SELECT * FROM tabla_que_no_existe")).asString().startsWith("Error SQL");
	}

}
