package com.mobilesco.mobilesco_back.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class NivelSchemaConfig {

    private static final Logger log = LoggerFactory.getLogger(NivelSchemaConfig.class);

    @Bean
    @Order(1)
    CommandLineRunner syncNivelUniqueIndexes(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return args -> {
            if (!isMysql(dataSource) || !tableExists(jdbcTemplate, "niveles")) {
                return;
            }

            ensureCompositeUniqueIndex(jdbcTemplate, "uk_nivel_modelo_codigo", List.of("producto_base_id", "codigo"));
            dropCodigoOnlyUniqueIndexes(jdbcTemplate);
        };
    }

    private boolean isMysql(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName().toLowerCase().contains("mysql");
        } catch (SQLException ex) {
            log.warn("No se pudo validar el motor de base de datos para revisar indices de niveles", ex);
            return false;
        }
    }

    private boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """,
                Integer.class,
                tableName);
        return count != null && count > 0;
    }

    private void ensureCompositeUniqueIndex(JdbcTemplate jdbcTemplate, String indexName, List<String> columns) {
        if (indexExists(jdbcTemplate, indexName)) {
            return;
        }

        String columnas = columns.stream()
                .map(column -> "`" + column + "`")
                .reduce((left, right) -> left + ", " + right)
                .orElseThrow();
        jdbcTemplate.execute("ALTER TABLE `niveles` ADD CONSTRAINT `" + indexName + "` UNIQUE (" + columnas + ")");
        log.info("Indice unico creado para niveles: {}", indexName);
    }

    private boolean indexExists(JdbcTemplate jdbcTemplate, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = 'niveles'
                  AND index_name = ?
                """,
                Integer.class,
                indexName);
        return count != null && count > 0;
    }

    private void dropCodigoOnlyUniqueIndexes(JdbcTemplate jdbcTemplate) {
        List<String> indexes = jdbcTemplate.queryForList(
                """
                SELECT s.index_name
                FROM information_schema.statistics s
                WHERE s.table_schema = DATABASE()
                  AND s.table_name = 'niveles'
                  AND s.non_unique = 0
                GROUP BY s.index_name
                HAVING COUNT(*) = 1
                   AND MAX(s.column_name) = 'codigo'
                """,
                String.class);

        for (String index : indexes) {
            jdbcTemplate.execute("ALTER TABLE `niveles` DROP INDEX `" + index + "`");
            log.info("Indice unico global eliminado de niveles.codigo: {}", index);
        }
    }
}
