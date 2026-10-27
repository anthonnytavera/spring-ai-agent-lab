package com.adtavera.spring_ai_agent_lab.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.adtavera.spring_ai_agent_lab.agent.DatabaseAgent;
import com.adtavera.spring_ai_agent_lab.web.dto.AskRequest;

/** Pruebas unitarias del controlador, con el agente simulado: no llaman al modelo ni arrancan Spring. */
class AgentControllerTest {

	private final DatabaseAgent agent = mock(DatabaseAgent.class);
	private final AgentController controller = new AgentController(agent);

	@Test
	void answersUsingTheGivenConversation() {
		when(agent.ask("ana", "hola")).thenReturn("respuesta");

		assertThat(controller.ask(new AskRequest("ana", "hola")).answer()).isEqualTo("respuesta");
	}

	@Test
	void usesDefaultConversationWhenNoneIsGiven() {
		when(agent.ask("default", "hola")).thenReturn("respuesta");

		controller.ask(new AskRequest(null, "hola"));
		controller.ask(new AskRequest("  ", "hola"));

		verify(agent, org.mockito.Mockito.times(2)).ask("default", "hola");
	}

	@Test
	void rejectsMissingOrBlankQuestion() {
		assertThatThrownBy(() -> controller.ask(new AskRequest("ana", null))).isInstanceOf(InvalidRequestException.class);
		assertThatThrownBy(() -> controller.ask(new AskRequest("ana", "   "))).isInstanceOf(InvalidRequestException.class);
	}

}
