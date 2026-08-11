package com.mobilesco.mobilesco_back.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class PermissionMigrationTest {

    @Test
    void migraAccesosLegacyYDependenciasSinDuplicarlos() throws Exception {
        String url = "jdbc:h2:mem:permissions_" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .target("5")
                .cleanDisabled(true)
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            seedLegacyAccess(connection);
        }

        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .cleanDisabled(true)
                .load();
        flyway.migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            assertThat(permissionCodes(connection, "role_permissions", "role_id", 1L))
                    .contains("VIEW_USERS", "ACTION_USER_ROLES", "ACTION_ROLES_PERMISSIONS")
                    .doesNotContain("ACTION_USERS_WRITE");
            assertThat(permissionCodes(connection, "user_permissions", "user_id", 1L))
                    .contains(
                            "VIEW_USERS",
                            "ACTION_USERS_CREATE",
                            "ACTION_USER_ROLES",
                            "ACTION_USER_PERMISSIONS",
                            "ACTION_USERS_STATUS",
                            "ACTION_ROLES_CREATE",
                            "ACTION_ROLES_PERMISSIONS",
                            "ACTION_STOCK_ADJUSTMENTS",
                            "VIEW_INVENTORY")
                    .doesNotContain("ACTION_USERS_WRITE");
            try (Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(
                            "SELECT activo FROM permissions WHERE code = 'ACTION_USERS_WRITE'")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getBoolean(1)).isFalse();
            }

            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO access_audit_logs
                        (fecha, accion, entidad, actor_email, detalle)
                    VALUES (CURRENT_TIMESTAMP, 'PRUEBA', 'USUARIO', 'qa@mobilesco.test', ?)
                    """)) {
                statement.setString(1, "x".repeat(3_000));
                assertThat(statement.executeUpdate()).isEqualTo(1);
            }
        }

        int assignmentsBefore = countAssignments(url);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(countAssignments(url)).isEqualTo(assignmentsBefore);
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("6");
    }

    private void seedLegacyAccess(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO roles (id, name, sistema) VALUES (1, 'ROL_LEGACY', FALSE)");
            statement.execute("""
                    INSERT INTO users
                        (id, email, password_hash, enabled, locked, account_status)
                    VALUES (1, 'legacy@mobilesco.test', 'hash', TRUE, FALSE, 'ACTIVE')
                    """);
        }

        List<String> codes = List.of(
                "VIEW_USERS",
                "ACTION_USERS_WRITE",
                "ACTION_STOCK_ADJUSTMENTS",
                "ACTION_USERS_CREATE",
                "ACTION_USER_ROLES",
                "ACTION_USER_PERMISSIONS",
                "ACTION_USERS_STATUS",
                "ACTION_ROLES_CREATE",
                "ACTION_ROLES_PERMISSIONS",
                "VIEW_INVENTORY");
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO permissions (id, activo, tipo, modulo, code, nombre)
                VALUES (?, TRUE, ?, 'Prueba', ?, ?)
                """)) {
            for (int index = 0; index < codes.size(); index++) {
                String code = codes.get(index);
                statement.setLong(1, index + 1L);
                statement.setString(2, code.startsWith("VIEW_") ? "VIEW" : "ACTION");
                statement.setString(3, code);
                statement.setString(4, code);
                statement.addBatch();
            }
            statement.executeBatch();
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1)");
            statement.execute("INSERT INTO user_permissions (user_id, permission_id) VALUES (1, 2)");
            statement.execute("INSERT INTO user_permissions (user_id, permission_id) VALUES (1, 3)");
        }
    }

    private List<String> permissionCodes(
            Connection connection,
            String joinTable,
            String subjectColumn,
            long subjectId) throws Exception {
        String sql = "SELECT p.code FROM " + joinTable + " j "
                + "JOIN permissions p ON p.id = j.permission_id "
                + "WHERE j." + subjectColumn + " = ? ORDER BY p.code";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, subjectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                var codes = new java.util.ArrayList<String>();
                while (resultSet.next()) {
                    codes.add(resultSet.getString(1));
                }
                return codes;
            }
        }
    }

    private int countAssignments(String url) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("""
                        SELECT
                            (SELECT COUNT(*) FROM role_permissions)
                          + (SELECT COUNT(*) FROM user_permissions)
                        """)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
