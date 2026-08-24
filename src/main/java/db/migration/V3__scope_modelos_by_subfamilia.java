package db.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Changes model uniqueness from family scope to family/subfamily scope. */
public class V3__scope_modelos_by_subfamilia extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String database = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);

        try (Statement statement = connection.createStatement()) {
            addUniqueConstraintIfMissing(connection, statement, "uk_modelo_clasificacion_codigo",
                    "familia_id, subfamilia_id, codigo");
            addUniqueConstraintIfMissing(connection, statement, "uk_modelo_clasificacion_nombre",
                    "familia_id, subfamilia_id, nombre");

            if (database.contains("mysql") || database.contains("mariadb")) {
                dropIndexIfExists(connection, statement, "uk_modelo_familia_codigo", "DROP INDEX");
                dropIndexIfExists(connection, statement, "uk_modelo_familia_nombre", "DROP INDEX");
            } else {
                dropIndexIfExists(connection, statement, "uk_modelo_familia_codigo", "DROP CONSTRAINT");
                dropIndexIfExists(connection, statement, "uk_modelo_familia_nombre", "DROP CONSTRAINT");
            }
        }
    }

    private void addUniqueConstraintIfMissing(Connection connection, Statement statement, String name, String columns)
            throws Exception {
        if (!indexExists(connection, name)) {
            statement.execute("""
                    ALTER TABLE productos_base
                    ADD CONSTRAINT %s
                    UNIQUE (%s)
                    """.formatted(name, columns));
        }
    }

    private void dropIndexIfExists(Connection connection, Statement statement, String name, String dropCommand)
            throws Exception {
        if (indexExists(connection, name)) {
            statement.execute("""
                    ALTER TABLE productos_base
                    %s %s
                    """.formatted(dropCommand, name));
        }
    }

    private boolean indexExists(Connection connection, String name) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet indexes = metaData.getIndexInfo(connection.getCatalog(), null, "productos_base", false, false)) {
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if (name.equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
