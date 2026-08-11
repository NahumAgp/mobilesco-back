package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.PermisoCatalog;

/**
 * Converts the one-off granular-permission compatibility work into a versioned
 * migration and repairs action assignments that lack their required view.
 */
public class V6__normalize_permissions_and_audit extends BaseJavaMigration {

    private static final Set<String> LEGACY_USER_WRITE_REPLACEMENTS = Set.of(
            "VIEW_USERS",
            "ACTION_USERS_CREATE",
            "ACTION_USER_ROLES",
            "ACTION_USER_PERMISSIONS",
            "ACTION_USERS_STATUS",
            "ACTION_ROLES_CREATE",
            "ACTION_ROLES_PERMISSIONS"
    );

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        ensurePermissionCatalog(connection);
        expandLegacyRolePermissions(connection);
        migrateLegacyUserWrite(connection, "role_permissions", "role_id");
        migrateLegacyUserWrite(connection, "user_permissions", "user_id");
        addRequiredViews(connection, "role_permissions", "role_id");
        addRequiredViews(connection, "user_permissions", "user_id");
        removeLegacyUserWriteAssignments(connection);
        deactivateRemovedPermissions(connection);
        widenAuditDetail(connection);
    }

    private void ensurePermissionCatalog(Connection connection) throws Exception {
        String updateSql = """
                UPDATE permissions
                   SET nombre = ?, modulo = ?, vista = ?, descripcion = ?, ruta = ?, tipo = ?, activo = TRUE
                 WHERE code = ?
                """;
        String insertSql = """
                INSERT INTO permissions
                    (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)
                """;

        try (PreparedStatement update = connection.prepareStatement(updateSql);
                PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (PermisoCatalog.Definition definition : PermisoCatalog.DEFINITIONS) {
                update.setString(1, definition.nombre());
                update.setString(2, definition.modulo());
                update.setString(3, definition.vista());
                update.setString(4, definition.descripcion());
                update.setString(5, definition.ruta());
                update.setString(6, definition.tipo());
                update.setString(7, definition.code());
                if (update.executeUpdate() > 0) {
                    continue;
                }

                insert.setString(1, definition.code());
                insert.setString(2, definition.nombre());
                insert.setString(3, definition.modulo());
                insert.setString(4, definition.vista());
                insert.setString(5, definition.descripcion());
                insert.setString(6, definition.ruta());
                insert.setString(7, definition.tipo());
                insert.executeUpdate();
            }
        }
    }

    private void expandLegacyRolePermissions(Connection connection) throws Exception {
        for (Map.Entry<String, Set<String>> expansion : PermisoCatalog.LEGACY_EXPANSIONS.entrySet()) {
            for (String targetCode : expansion.getValue()) {
                grantToCurrentHolders(
                        connection,
                        "role_permissions",
                        "role_id",
                        expansion.getKey(),
                        targetCode);
            }
        }
    }

    private void migrateLegacyUserWrite(
            Connection connection,
            String joinTable,
            String subjectColumn) throws Exception {
        for (String replacement : LEGACY_USER_WRITE_REPLACEMENTS) {
            grantToCurrentHolders(
                    connection,
                    joinTable,
                    subjectColumn,
                    "ACTION_USERS_WRITE",
                    replacement);
        }
    }

    private void addRequiredViews(
            Connection connection,
            String joinTable,
            String subjectColumn) throws Exception {
        for (PermisoCatalog.Definition definition : PermisoCatalog.DEFINITIONS) {
            if (definition.vistaRequerida() != null) {
                grantToCurrentHolders(
                        connection,
                        joinTable,
                        subjectColumn,
                        definition.code(),
                        definition.vistaRequerida());
            }
        }
    }

    private void grantToCurrentHolders(
            Connection connection,
            String joinTable,
            String subjectColumn,
            String sourceCode,
            String targetCode) throws Exception {
        String sql = """
                INSERT INTO %s (%s, permission_id)
                SELECT DISTINCT source_assignment.%s, target_permission.id
                FROM %s source_assignment
                JOIN permissions source_permission
                  ON source_permission.id = source_assignment.permission_id
                 AND source_permission.code = ?
                JOIN permissions target_permission
                  ON target_permission.code = ?
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM %s existing_assignment
                    WHERE existing_assignment.%s = source_assignment.%s
                      AND existing_assignment.permission_id = target_permission.id
                )
                """.formatted(
                joinTable,
                subjectColumn,
                subjectColumn,
                joinTable,
                joinTable,
                subjectColumn,
                subjectColumn);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sourceCode);
            statement.setString(2, targetCode);
            statement.executeUpdate();
        }
    }

    private void removeLegacyUserWriteAssignments(Connection connection) throws Exception {
        for (String joinTable : Set.of("role_permissions", "user_permissions")) {
            String sql = "DELETE FROM " + joinTable
                    + " WHERE permission_id IN (SELECT id FROM permissions WHERE code = ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, "ACTION_USERS_WRITE");
                statement.executeUpdate();
            }
        }
    }

    private void deactivateRemovedPermissions(Connection connection) throws Exception {
        String placeholders = String.join(", ",
                Collections.nCopies(PermisoCatalog.ALL_CODES.size(), "?"));
        String sql = "UPDATE permissions SET activo = FALSE WHERE code NOT IN (" + placeholders + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (String code : PermisoCatalog.ALL_CODES.stream().sorted().toList()) {
                statement.setString(index++, code);
            }
            statement.executeUpdate();
        }
    }

    private void widenAuditDetail(Connection connection) throws Exception {
        String database = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
        String sql = database.contains("mysql") || database.contains("mariadb")
                ? "ALTER TABLE access_audit_logs MODIFY COLUMN detalle TEXT NULL"
                : "ALTER TABLE access_audit_logs ALTER COLUMN detalle TEXT";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
