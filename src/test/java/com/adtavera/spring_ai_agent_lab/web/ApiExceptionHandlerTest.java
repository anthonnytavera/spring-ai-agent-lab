package com.adtavera.spring_ai_agent_lab.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.adtavera.spring_ai_agent_lab.agent.AgentUnavailableException;
import com.adtavera.spring_ai_agent_lab.web.dto.ErrorResponse;

/** Comprueba que cada tipo de fallo se traduce al código HTTP correcto y sin filtrar detalles internos. */
class ApiExceptionHandlerTest {

	private final ApiExceptionHandler handler = new ApiExceptionHandler();

	@Test
	void invalidRequestIsABadRequest() {
		ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(new InvalidRequestException("falta question"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().message()).isEqualTo("falta question");
	}

	@Test
	void providerFailureIsABadGatewayAndDoesNotLeakTheInternalCause() {
		AgentUnavailableException e = new AgentUnavailableException("fallo",
				new RuntimeException("402: Insufficient account funds"));

		ResponseEntity<ErrorResponse> response = handler.handleAgentUnavailable(e);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
		assertThat(response.getBody().message()).doesNotContain("402").doesNotContain("Insufficient");
	}

}
