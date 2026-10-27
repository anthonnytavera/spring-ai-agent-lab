package com.adtavera.spring_ai_agent_lab.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

/**
 * Pruebas de la defensa del acceso a datos. No llaman al modelo: atacan directamente a
 * {@link SafeQueryExecutor}, como lo haría un SQL malicioso generado por un modelo manipulado.
 */
@SpringBootTest(properties = "spring.ai.openai.api-key=test-key")
class SafeQueryExecutorTest {

	@Autowired
	SafeQueryExecutor executor;

	@Value("${agent.datasource.url}")
	String url;

	@Value("${agent.datasource.username}")
	String username;

	@Value("${agent.datasource.password}")
	String password;

	@Test
	void listColumnsShowsTablesVisibleToTheReadOnlyUser() {
		List<Map<String, Object>> columns = executor.listColumns();

		assertThat(columns).extracting(c -> c.get("TABLE_NAME")).contains("CUSTOMERS", "PRODUCTS", "ORDERS");
	}

	@Test
	void selectReturnsRows() {
		List<Map<String, Object>> rows = executor.executeSelect("SELECT COUNT(*) AS total FROM customers");

		assertThat(rows).hasSize(1);
		assertThat(((Number) rows.get(0).get("TOTAL")).intValue()).isEqualTo(4);
	}

	@Test
	void resultIsLimitedToMaxRows() {
		List<Map<String, Object>> rows = executor.executeSelect("SELECT * FROM SYSTEM_RANGE(1, 1000)");

		assertThat(rows).hasSize(SafeQueryExecutor.MAX_ROWS);
	}

	@Test
	void filterRejectsWriteStatements() {
		assertThatThrownBy(() -> executor.executeSelect("DROP TABLE customers"))
				.isInstanceOf(QueryRejectedException.class);
		assertThatThrownBy(() -> executor.executeSelect("SELECT 1; DELETE FROM orders"))
				.isInstanceOf(QueryRejectedException.class);
	}

	@Test
	void databaseRejectsWritesEvenWithoutTheFilter() throws SQLException {
		// Se salta el filtro: conexión directa con el usuario del agente. La defensa real es la base de datos.
		try (Connection c = DriverManager.getConnection(url, username, password)) {
			assertThatThrownBy(() -> c.createStatement().executeUpdate("DELETE FROM orders"))
					.isInstanceOf(SQLException.class);
			assertThatThrownBy(() -> c.createStatement().executeUpdate("DROP TABLE customers"))
					.isInstanceOf(SQLException.class);
			assertThatThrownBy(() -> c.createStatement().executeUpdate("CREATE USER hacker PASSWORD 'x' ADMIN"))
					.isInstanceOf(SQLException.class);
		}
	}

	@Test
	void fileReadingFunctionsAreNotAvailable() {
		assertThatThrownBy(() -> executor.executeSelect("SELECT * FROM CSVREAD('pom.xml')"))
				.isInstanceOf(DataAccessException.class);
		assertThatThrownBy(() -> executor.executeSelect("SELECT FILE_READ('pom.xml')"))
				.isInstanceOf(DataAccessException.class);
	}

}
