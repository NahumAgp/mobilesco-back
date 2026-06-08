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
public class DatabaseCollationConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCollationConfig.class);

    private static final String TARGET_CHARSET = "utf8mb4";
    private static final String TARGET_COLLATION = "utf8mb4_unicode_ci";

    @Bean
    @Order(0)
    CommandLineRunner syncMysqlCollations(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return args -> {
            if (!isMysql(dataSource)) {
                return;
            }

            List<ColumnCollationSpec> columns = List.of(
                    new ColumnCollationSpec("proveedor", "tipo_insumo", "VARCHAR(80)", true),
                    new ColumnCollationSpec("tipo_insumo_catalogo", "codigo", "VARCHAR(80)", false),
                    new ColumnCollationSpec("tipo_insumo_catalogo", "nombre", "VARCHAR(120)", false),
                    new ColumnCollationSpec("tipo_insumo_catalogo", "nombre_normalizado", "VARCHAR(120)", false),
                    new ColumnCollationSpec("tipo_insumo_catalogo", "descripcion", "VARCHAR(255)", true)
            );

            for (ColumnCollationSpec spec : columns) {
                syncColumnCollation(jdbcTemplate, spec);
            }
        };
    }

    private boolean isMysql(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName().toLowerCase().contains("mysql");
        } catch (SQLException ex) {
            log.warn("No se pudo validar el motor de base de datos para revisar collations", ex);
            return false;
        }
    }

    private void syncColumnCollation(JdbcTemplate jdbcTemplate, ColumnCollationSpec spec) {
        String currentCollation = jdbcTemplate.query(
                """
                SELECT c.collation_name
                FROM information_schema.columns c
                WHERE c.table_schema = DATABASE()
                  AND c.table_name = ?
                  AND c.column_name = ?
                """,
                rs -> rs.next() ? rs.getString(1) : null,
                spec.tableName(),
                spec.columnName());

        if (currentCollation == null || TARGET_COLLATION.equalsIgnoreCase(currentCollation)) {
            return;
        }

        String sql = String.format(
                "ALTER TABLE `%s` MODIFY COLUMN `%s` %s CHARACTER SET %s COLLATE %s %s",
                spec.tableName(),
                spec.columnName(),
                spec.sqlType(),
                TARGET_CHARSET,
                TARGET_COLLATION,
                spec.nullable() ? "NULL" : "NOT NULL");

        jdbcTemplate.execute(sql);
        log.info(
                "Collation alineada para {}.{}: {} -> {}",
                spec.tableName(),
                spec.columnName(),
                currentCollation,
                TARGET_COLLATION);
    }

    private record ColumnCollationSpec(
            String tableName,
            String columnName,
            String sqlType,
            boolean nullable
    ) {}
}
