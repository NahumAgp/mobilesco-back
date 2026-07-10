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
public class ProductTaxonomySchemaConfig {

    private static final Logger log = LoggerFactory.getLogger(ProductTaxonomySchemaConfig.class);

    @Bean
    @Order(1)
    CommandLineRunner syncProductTaxonomyUniqueIndexes(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return args -> {
            if (!isMysql(dataSource)) {
                return;
            }

            if (tableExists(jdbcTemplate, "familias")) {
                ensureNoDuplicates(jdbcTemplate, "familias", "linea_id", "codigo");
                ensureNoDuplicates(jdbcTemplate, "familias", "linea_id", "nombre");
                dropSingleColumnUniqueIndexes(jdbcTemplate, "familias", "codigo");
                dropSingleColumnUniqueIndexes(jdbcTemplate, "familias", "nombre");
                ensureCompositeUniqueIndex(jdbcTemplate, "familias", "uk_familia_linea_codigo", List.of("linea_id", "codigo"));
                ensureCompositeUniqueIndex(jdbcTemplate, "familias", "uk_familia_linea_nombre", List.of("linea_id", "nombre"));
            }

            if (tableExists(jdbcTemplate, "productos_base")) {
                ensureNoDuplicates(jdbcTemplate, "productos_base", "familia_id", "codigo");
                ensureNoDuplicates(jdbcTemplate, "productos_base", "familia_id", "nombre");
                dropSingleColumnUniqueIndexes(jdbcTemplate, "productos_base", "codigo");
                dropSingleColumnUniqueIndexes(jdbcTemplate, "productos_base", "nombre");
                ensureCompositeUniqueIndex(jdbcTemplate, "productos_base", "uk_modelo_familia_codigo", List.of("familia_id", "codigo"));
                ensureCompositeUniqueIndex(jdbcTemplate, "productos_base", "uk_modelo_familia_nombre", List.of("familia_id", "nombre"));
            }
        };
    }

    private boolean isMysql(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName().toLowerCase().contains("mysql");
        } catch (SQLException ex) {
            log.warn("No se pudo validar el motor de base de datos para revisar indices de catalogo de productos", ex);
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

    private void ensureNoDuplicates(JdbcTemplate jdbcTemplate, String tableName, String parentColumn, String valueColumn) {
        List<String> duplicates = jdbcTemplate.queryForList(
                """
                SELECT CONCAT(parent_key, ' / ', value_key, ' (', duplicate_count, ')') AS duplicate_key
                FROM (
                    SELECT `%1$s` AS parent_key,
                           LOWER(`%2$s`) AS value_key,
                           COUNT(*) AS duplicate_count
                    FROM `%3$s`
                    WHERE `%1$s` IS NOT NULL
                      AND `%2$s` IS NOT NULL
                    GROUP BY `%1$s`, LOWER(`%2$s`)
                    HAVING COUNT(*) > 1
                ) duplicate_groups
                LIMIT 5
                """.formatted(parentColumn, valueColumn, tableName),
                String.class);

        if (!duplicates.isEmpty()) {
            throw new IllegalStateException(
                    "No se pueden ajustar indices de " + tableName
                            + " porque existen duplicados dentro del mismo padre para " + valueColumn
                            + ": " + String.join(", ", duplicates));
        }
    }

    private void dropSingleColumnUniqueIndexes(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        List<String> indexes = jdbcTemplate.queryForList(
                """
                SELECT s.index_name
                FROM information_schema.statistics s
                WHERE s.table_schema = DATABASE()
                  AND s.table_name = ?
                  AND s.non_unique = 0
                  AND s.index_name <> 'PRIMARY'
                GROUP BY s.index_name
                HAVING COUNT(*) = 1
                   AND MAX(s.column_name) = ?
                """,
                String.class,
                tableName,
                columnName);

        for (String index : indexes) {
            jdbcTemplate.execute("ALTER TABLE `" + tableName + "` DROP INDEX `" + index + "`");
            log.info("Indice unico global eliminado de {}.{}: {}", tableName, columnName, index);
        }
    }

    private void ensureCompositeUniqueIndex(
            JdbcTemplate jdbcTemplate,
            String tableName,
            String indexName,
            List<String> columns) {
        if (indexExists(jdbcTemplate, tableName, indexName) || uniqueIndexOnColumnsExists(jdbcTemplate, tableName, columns)) {
            return;
        }

        String columnas = columns.stream()
                .map(column -> "`" + column + "`")
                .reduce((left, right) -> left + ", " + right)
                .orElseThrow();
        jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD CONSTRAINT `" + indexName + "` UNIQUE (" + columnas + ")");
        log.info("Indice unico compuesto creado para {}: {}", tableName, indexName);
    }

    private boolean indexExists(JdbcTemplate jdbcTemplate, String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND index_name = ?
                """,
                Integer.class,
                tableName,
                indexName);
        return count != null && count > 0;
    }

    private boolean uniqueIndexOnColumnsExists(JdbcTemplate jdbcTemplate, String tableName, List<String> columns) {
        String joinedColumns = String.join(",", columns);
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM (
                    SELECT s.index_name,
                           GROUP_CONCAT(s.column_name ORDER BY s.seq_in_index SEPARATOR ',') AS columns_key
                    FROM information_schema.statistics s
                    WHERE s.table_schema = DATABASE()
                      AND s.table_name = ?
                      AND s.non_unique = 0
                    GROUP BY s.index_name
                ) indexes_for_table
                WHERE columns_key = ?
                """,
                Integer.class,
                tableName,
                joinedColumns);
        return count != null && count > 0;
    }
}
