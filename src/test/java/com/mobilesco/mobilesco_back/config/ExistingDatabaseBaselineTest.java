package com.mobilesco.mobilesco_back.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;

class ExistingDatabaseBaselineTest {

    @Test
    void existingCompatibleSchemaIsBaselinedAndThenVerified() throws Exception {
        String url = newDatabaseUrl();
        createExistingSchemaWithoutFlywayHistory(url);

        Flyway flyway = baselineFlyway(url);
        flyway.migrate();

        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("8");
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT COUNT(*) FROM flyway_schema_history WHERE type = 'BASELINE' AND version = '1'")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(1);
        }
    }

    @Test
    void existingIncompleteSchemaFailsWithoutAutomaticRepair() throws Exception {
        String url = newDatabaseUrl();
        createExistingSchemaWithoutFlywayHistory(url);
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE notificacion");
        }

        Flyway flyway = baselineFlyway(url);

        assertThatThrownBy(flyway::migrate)
                .isInstanceOf(FlywayException.class)
                .hasStackTraceContaining("missing table `notificacion`")
                .hasStackTraceContaining("No changes were made");
    }

    private void createExistingSchemaWithoutFlywayHistory(String url) throws Exception {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .target("1")
                .cleanDisabled(true)
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            // This database is isolated and in-memory. Removing only Flyway metadata
            // simulates a legacy installation whose schema predates Flyway adoption.
            statement.execute("DROP TABLE flyway_schema_history");
        }
    }

    private Flyway baselineFlyway(String url) {
        return Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .cleanDisabled(true)
                .load();
    }

    private String newDatabaseUrl() {
        return "jdbc:h2:mem:baseline_" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }
}
