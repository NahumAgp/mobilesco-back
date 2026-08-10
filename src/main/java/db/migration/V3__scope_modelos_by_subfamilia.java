package db.migration;

import java.sql.Connection;
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
            if (database.contains("mysql") || database.contains("mariadb")) {
                statement.execute("ALTER TABLE productos_base DROP INDEX uk_modelo_familia_codigo");
                statement.execute("ALTER TABLE productos_base DROP INDEX uk_modelo_familia_nombre");
            } else {
                statement.execute("ALTER TABLE productos_base DROP CONSTRAINT uk_modelo_familia_codigo");
                statement.execute("ALTER TABLE productos_base DROP CONSTRAINT uk_modelo_familia_nombre");
            }

            statement.execute("""
                    ALTER TABLE productos_base
                    ADD CONSTRAINT uk_modelo_clasificacion_codigo
                    UNIQUE (familia_id, subfamilia_id, codigo)
                    """);
            statement.execute("""
                    ALTER TABLE productos_base
                    ADD CONSTRAINT uk_modelo_clasificacion_nombre
                    UNIQUE (familia_id, subfamilia_id, nombre)
                    """);
        }
    }
}
