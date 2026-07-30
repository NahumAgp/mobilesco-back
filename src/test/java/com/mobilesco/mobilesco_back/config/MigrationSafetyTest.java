package com.mobilesco.mobilesco_back.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class MigrationSafetyTest {

    private static final List<Pattern> DESTRUCTIVE_SQL = List.of(
            Pattern.compile("\\bdrop\\s+table\\b"),
            Pattern.compile("\\bdrop\\s+column\\b"),
            Pattern.compile("\\btruncate\\s+table\\b"),
            Pattern.compile("\\bdelete\\s+from\\b"));

    @Test
    void initialMigrationsContainNoDestructiveStatements() throws IOException {
        String migration = new ClassPathResource("db/migration/V1__initial_schema.sql")
                .getContentAsString(StandardCharsets.UTF_8)
                .toLowerCase(Locale.ROOT);

        assertThat(DESTRUCTIVE_SQL)
                .allSatisfy(pattern -> assertThat(pattern.matcher(migration).find())
                        .as("migration must not contain %s", pattern)
                        .isFalse());
    }

    @Test
    void everyRuntimeProfileDelegatesSchemaChangesToFlyway() throws IOException {
        for (String resource : List.of(
                "application.properties",
                "application-dev.properties",
                "application-prod.properties")) {
            String properties = new ClassPathResource(resource)
                    .getContentAsString(StandardCharsets.UTF_8);
            assertThat(properties)
                    .as(resource)
                    .doesNotContain("ddl-auto=update", "ddl-auto=create", "ddl-auto=create-drop");
        }
    }
}
