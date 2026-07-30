package db.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Read-only gate for both greenfield databases and installations baselined at V1.
 *
 * <p>This migration deliberately does not repair an existing database. A mismatch
 * stops deployment so an operator can inspect a backup and prepare an explicit,
 * reviewed forward migration.</p>
 */
public class V2__verify_baseline_schema extends BaseJavaMigration {

    private static final Set<String> REQUIRED_TABLES = Set.of(
            "access_audit_logs",
            "areas_trabajo",
            "categoria",
            "centro_trabajo",
            "cif_configuracion",
            "cliente",
            "colores",
            "compra",
            "costo_indirecto",
            "cotizacion",
            "cotizacion_detalle",
            "cuenta_por_pagar",
            "detalle_compra",
            "detalle_salida_insumo",
            "empleados",
            "familias",
            "imagenes",
            "insumo",
            "kardex",
            "linea_producto",
            "lineas",
            "material",
            "modelo_insumo",
            "modelo_material",
            "modelo_operacion",
            "niveles",
            "notificacion",
            "operacion",
            "pago_cuenta_por_pagar",
            "permissions",
            "producto",
            "producto_insumo",
            "producto_operacion",
            "productos_base",
            "proveedor",
            "refresh_tokens",
            "requisicion_almacen",
            "requisicion_almacen_detalle",
            "role_permissions",
            "roles",
            "salida_insumo",
            "subfamilias",
            "tipo_insumo_catalogo",
            "unidad_medida",
            "user_invitations",
            "user_permissions",
            "user_roles",
            "users"
    );

    private static final Map<String, Set<String>> REQUIRED_COLUMNS = Map.of(
            "cliente", Set.of("codigo", "clasificacion", "tipo_persona"),
            "modelo_insumo", Set.of("modelo_id", "insumo_id"),
            "modelo_operacion", Set.of("modelo_id", "operacion_id", "orden"),
            "notificacion", Set.of("destinatario_usuario_id", "leida", "fecha_creacion"),
            "requisicion_almacen", Set.of("folio", "solicitante_usuario_id", "estado"),
            "requisicion_almacen_detalle", Set.of("requisicion_id", "insumo_id", "cantidad_solicitada"),
            "salida_insumo", Set.of("tipo_salida", "orden_produccion"),
            "tipo_insumo_catalogo", Set.of("codigo", "nombre", "nombre_normalizado")
    );

    private static final Map<ColumnRef, Boolean> EXPECTED_NULLABILITY = Map.of(
            new ColumnRef("salida_insumo", "tipo_salida"), false,
            new ColumnRef("salida_insumo", "orden_produccion"), true,
            new ColumnRef("producto_insumo", "cantidad"), true,
            new ColumnRef("producto_operacion", "cantidad"), true
    );

    private static final Map<String, List<List<String>>> REQUIRED_UNIQUE_INDEXES = Map.of(
            "familias", List.of(List.of("linea_id", "codigo"), List.of("linea_id", "nombre")),
            "niveles", List.of(List.of("producto_base_id", "codigo")),
            "productos_base", List.of(List.of("familia_id", "codigo"), List.of("familia_id", "nombre")),
            "requisicion_almacen_detalle", List.of(List.of("requisicion_id", "insumo_id"))
    );

    private static final List<ColumnRef> UTF8MB4_COLUMNS = List.of(
            new ColumnRef("proveedor", "tipo_insumo"),
            new ColumnRef("tipo_insumo_catalogo", "codigo"),
            new ColumnRef("tipo_insumo_catalogo", "nombre"),
            new ColumnRef("tipo_insumo_catalogo", "nombre_normalizado"),
            new ColumnRef("tipo_insumo_catalogo", "descripcion")
    );

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        DatabaseMetaData metadata = connection.getMetaData();
        Map<String, String> actualTables = readTables(connection, metadata);
        List<String> problems = new ArrayList<>();

        REQUIRED_TABLES.stream()
                .sorted()
                .filter(table -> !actualTables.containsKey(table))
                .forEach(table -> problems.add("missing table `" + table + "`"));

        for (Map.Entry<String, Set<String>> requirement : REQUIRED_COLUMNS.entrySet()) {
            String actualTable = actualTables.get(requirement.getKey());
            if (actualTable == null) {
                continue;
            }
            Map<String, ColumnMetadata> columns = readColumns(connection, metadata, actualTable);
            requirement.getValue().stream()
                    .sorted()
                    .filter(column -> !columns.containsKey(column))
                    .forEach(column -> problems.add(
                            "missing column `" + requirement.getKey() + "`.`" + column + "`"));
        }

        for (Map.Entry<ColumnRef, Boolean> expectation : EXPECTED_NULLABILITY.entrySet()) {
            ColumnRef ref = expectation.getKey();
            String actualTable = actualTables.get(ref.table());
            if (actualTable == null) {
                continue;
            }
            ColumnMetadata column = readColumns(connection, metadata, actualTable).get(ref.column());
            if (column != null && column.nullable() != expectation.getValue()) {
                problems.add("column `" + ref.table() + "`.`" + ref.column() + "` must be "
                        + (expectation.getValue() ? "nullable" : "NOT NULL"));
            }
        }

        for (Map.Entry<String, List<List<String>>> requirement : REQUIRED_UNIQUE_INDEXES.entrySet()) {
            String actualTable = actualTables.get(requirement.getKey());
            if (actualTable == null) {
                continue;
            }
            List<List<String>> uniqueIndexes = readUniqueIndexes(connection, metadata, actualTable);
            for (List<String> expectedColumns : requirement.getValue()) {
                if (!uniqueIndexes.contains(expectedColumns)) {
                    problems.add("missing unique index on `" + requirement.getKey() + "` ("
                            + String.join(", ", expectedColumns) + ")");
                }
            }
        }

        if (metadata.getDatabaseProductName().toLowerCase(Locale.ROOT).contains("mysql")) {
            validateMysqlCollations(connection, problems);
        }

        if (!problems.isEmpty()) {
            throw new FlywayException(
                    "Baseline schema verification failed. No changes were made. "
                            + "Restore/upgrade the schema with a reviewed forward migration before retrying: "
                            + String.join("; ", problems));
        }
    }

    private Map<String, String> readTables(Connection connection, DatabaseMetaData metadata) throws SQLException {
        Map<String, String> tables = new HashMap<>();
        try (ResultSet resultSet = metadata.getTables(
                connection.getCatalog(), connection.getSchema(), "%", new String[] {"TABLE"})) {
            while (resultSet.next()) {
                String name = resultSet.getString("TABLE_NAME");
                tables.put(name.toLowerCase(Locale.ROOT), name);
            }
        }
        return tables;
    }

    private Map<String, ColumnMetadata> readColumns(
            Connection connection, DatabaseMetaData metadata, String table) throws SQLException {
        Map<String, ColumnMetadata> columns = new HashMap<>();
        try (ResultSet resultSet = metadata.getColumns(
                connection.getCatalog(), connection.getSchema(), table, "%")) {
            while (resultSet.next()) {
                String name = resultSet.getString("COLUMN_NAME").toLowerCase(Locale.ROOT);
                boolean nullable = resultSet.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                columns.put(name, new ColumnMetadata(nullable));
            }
        }
        return columns;
    }

    private List<List<String>> readUniqueIndexes(
            Connection connection, DatabaseMetaData metadata, String table) throws SQLException {
        Map<String, List<IndexedColumn>> indexes = new LinkedHashMap<>();
        try (ResultSet resultSet = metadata.getIndexInfo(
                connection.getCatalog(), connection.getSchema(), table, true, false)) {
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                if (indexName == null || columnName == null) {
                    continue;
                }
                indexes.computeIfAbsent(indexName, ignored -> new ArrayList<>())
                        .add(new IndexedColumn(
                                resultSet.getInt("ORDINAL_POSITION"),
                                columnName.toLowerCase(Locale.ROOT)));
            }
        }

        List<List<String>> result = new ArrayList<>();
        for (List<IndexedColumn> columns : indexes.values()) {
            columns.sort(Comparator.comparingInt(IndexedColumn::position));
            result.add(columns.stream().map(IndexedColumn::name).toList());
        }
        return result;
    }

    private void validateMysqlCollations(Connection connection, List<String> problems) throws SQLException {
        String sql = """
                SELECT collation_name, data_type
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ColumnRef ref : UTF8MB4_COLUMNS) {
                statement.setString(1, ref.table());
                statement.setString(2, ref.column());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        continue;
                    }
                    String collation = resultSet.getString("collation_name");
                    int jdbcType = jdbcType(resultSet.getString("data_type"));
                    if (isCharacterType(jdbcType)
                            && (collation == null || !collation.equalsIgnoreCase("utf8mb4_unicode_ci"))) {
                        problems.add("column `" + ref.table() + "`.`" + ref.column()
                                + "` must use utf8mb4_unicode_ci");
                    }
                }
            }
        }
    }

    private int jdbcType(String mysqlType) {
        return switch (mysqlType.toLowerCase(Locale.ROOT)) {
            case "char" -> Types.CHAR;
            case "varchar" -> Types.VARCHAR;
            case "text", "tinytext", "mediumtext", "longtext" -> Types.LONGVARCHAR;
            default -> Types.OTHER;
        };
    }

    private boolean isCharacterType(int jdbcType) {
        return jdbcType == Types.CHAR || jdbcType == Types.VARCHAR || jdbcType == Types.LONGVARCHAR;
    }

    private record ColumnRef(String table, String column) {}

    private record ColumnMetadata(boolean nullable) {}

    private record IndexedColumn(int position, String name) {}
}
