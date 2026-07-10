package com.mobilesco.mobilesco_back.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SalidaInsumoSchemaConfig {

    private static final Logger log = LoggerFactory.getLogger(SalidaInsumoSchemaConfig.class);

    @Bean
    @Order(1)
    CommandLineRunner syncSalidaInsumoSchema(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return args -> {
            if (!isMysqlLike(dataSource) || !tableExists(jdbcTemplate, "salida_insumo")) {
                return;
            }

            if (!columnExists(jdbcTemplate, "salida_insumo", "tipo_salida")) {
                jdbcTemplate.execute(
                        "ALTER TABLE `salida_insumo` ADD COLUMN `tipo_salida` VARCHAR(20) NOT NULL DEFAULT 'DIRECTA' AFTER `id`");
                log.info("Columna salida_insumo.tipo_salida creada");
            }

            if (!isColumnNullable(jdbcTemplate, "salida_insumo", "orden_produccion")) {
                jdbcTemplate.execute("ALTER TABLE `salida_insumo` MODIFY COLUMN `orden_produccion` VARCHAR(100) NULL");
                log.info("Columna salida_insumo.orden_produccion ajustada para permitir salidas indirectas");
            }
        };
    }

    private boolean isMysqlLike(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName().toLowerCase();
            return productName.contains("mysql") || productName.contains("mariadb");
        } catch (SQLException ex) {
            log.warn("No se pudo validar el motor de base de datos para salida_insumo", ex);
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

    private boolean columnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }

    private boolean isColumnNullable(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        String nullable = jdbcTemplate.query(
                """
                SELECT is_nullable
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """,
                rs -> rs.next() ? rs.getString(1) : null,
                tableName,
                columnName);
        return "YES".equalsIgnoreCase(nullable);
    }
}
