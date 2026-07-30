package com.mobilesco.mobilesco_back;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MobilescoBackApplication.class)
@ActiveProfiles("test")
class MobilescoBackApplicationTests {

	@Autowired
	private Flyway flyway;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoadsWithMigratedAndValidatedSchema() {
		assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_NAME) = 'flyway_schema_history'",
				Integer.class)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM permissions", Integer.class)).isPositive();
	}

}
