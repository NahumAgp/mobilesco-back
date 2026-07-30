-- PRECHECK DE SOLO LECTURA PARA MYSQL 8.
-- Cada consulta debe devolver cero filas antes de activar FLYWAY_BASELINE_ON_MIGRATE.
-- Este archivo no crea, altera ni elimina objetos o datos.

-- La base no debe haber sido administrada previamente por Flyway.
SELECT table_name AS problema
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'flyway_schema_history';

-- Deben existir todas las tablas representadas por la version baseline.
WITH expected_table (table_name) AS (
    SELECT 'access_audit_logs' UNION ALL
    SELECT 'areas_trabajo' UNION ALL
    SELECT 'categoria' UNION ALL
    SELECT 'centro_trabajo' UNION ALL
    SELECT 'cif_configuracion' UNION ALL
    SELECT 'cliente' UNION ALL
    SELECT 'colores' UNION ALL
    SELECT 'compra' UNION ALL
    SELECT 'costo_indirecto' UNION ALL
    SELECT 'cotizacion' UNION ALL
    SELECT 'cotizacion_detalle' UNION ALL
    SELECT 'cuenta_por_pagar' UNION ALL
    SELECT 'detalle_compra' UNION ALL
    SELECT 'detalle_salida_insumo' UNION ALL
    SELECT 'empleados' UNION ALL
    SELECT 'familias' UNION ALL
    SELECT 'imagenes' UNION ALL
    SELECT 'insumo' UNION ALL
    SELECT 'kardex' UNION ALL
    SELECT 'linea_producto' UNION ALL
    SELECT 'lineas' UNION ALL
    SELECT 'material' UNION ALL
    SELECT 'modelo_insumo' UNION ALL
    SELECT 'modelo_material' UNION ALL
    SELECT 'modelo_operacion' UNION ALL
    SELECT 'niveles' UNION ALL
    SELECT 'notificacion' UNION ALL
    SELECT 'operacion' UNION ALL
    SELECT 'pago_cuenta_por_pagar' UNION ALL
    SELECT 'permissions' UNION ALL
    SELECT 'producto' UNION ALL
    SELECT 'producto_insumo' UNION ALL
    SELECT 'producto_operacion' UNION ALL
    SELECT 'productos_base' UNION ALL
    SELECT 'proveedor' UNION ALL
    SELECT 'refresh_tokens' UNION ALL
    SELECT 'requisicion_almacen' UNION ALL
    SELECT 'requisicion_almacen_detalle' UNION ALL
    SELECT 'role_permissions' UNION ALL
    SELECT 'roles' UNION ALL
    SELECT 'salida_insumo' UNION ALL
    SELECT 'subfamilias' UNION ALL
    SELECT 'tipo_insumo_catalogo' UNION ALL
    SELECT 'unidad_medida' UNION ALL
    SELECT 'user_invitations' UNION ALL
    SELECT 'user_permissions' UNION ALL
    SELECT 'user_roles' UNION ALL
    SELECT 'users'
)
SELECT CONCAT('Falta tabla: ', expected_table.table_name) AS problema
FROM expected_table
LEFT JOIN information_schema.tables actual
       ON actual.table_schema = DATABASE()
      AND actual.table_name = expected_table.table_name
WHERE actual.table_name IS NULL;

-- Columnas recientes y nulabilidad esperada.
WITH expected_column (table_name, column_name, is_nullable) AS (
    SELECT 'cliente', 'clasificacion', 'NO' UNION ALL
    SELECT 'cliente', 'tipo_persona', 'NO' UNION ALL
    SELECT 'modelo_insumo', 'modelo_id', 'NO' UNION ALL
    SELECT 'modelo_insumo', 'insumo_id', 'NO' UNION ALL
    SELECT 'modelo_operacion', 'modelo_id', 'NO' UNION ALL
    SELECT 'modelo_operacion', 'operacion_id', 'NO' UNION ALL
    SELECT 'notificacion', 'destinatario_usuario_id', 'NO' UNION ALL
    SELECT 'requisicion_almacen', 'folio', 'NO' UNION ALL
    SELECT 'requisicion_almacen_detalle', 'requisicion_id', 'NO' UNION ALL
    SELECT 'salida_insumo', 'tipo_salida', 'NO' UNION ALL
    SELECT 'salida_insumo', 'orden_produccion', 'YES' UNION ALL
    SELECT 'producto_insumo', 'cantidad', 'YES' UNION ALL
    SELECT 'producto_operacion', 'cantidad', 'YES' UNION ALL
    SELECT 'tipo_insumo_catalogo', 'nombre_normalizado', 'NO'
)
SELECT CONCAT(
           IF(actual.column_name IS NULL, 'Falta columna: ', 'Nulabilidad incorrecta: '),
           expected_column.table_name, '.', expected_column.column_name,
           IF(actual.column_name IS NULL, '', CONCAT(' (esperada ', expected_column.is_nullable, ')'))
       ) AS problema
FROM expected_column
LEFT JOIN information_schema.columns actual
       ON actual.table_schema = DATABASE()
      AND actual.table_name = expected_column.table_name
      AND actual.column_name = expected_column.column_name
WHERE actual.column_name IS NULL
   OR actual.is_nullable <> expected_column.is_nullable;

-- Estos grupos impedirian crear los indices unicos compuestos actuales.
SELECT 'Duplicado familias(linea_id,codigo)' AS problema, linea_id, LOWER(codigo) AS valor, COUNT(*) AS repeticiones
FROM familias
WHERE linea_id IS NOT NULL AND codigo IS NOT NULL
GROUP BY linea_id, LOWER(codigo)
HAVING COUNT(*) > 1;

SELECT 'Duplicado familias(linea_id,nombre)' AS problema, linea_id, LOWER(nombre) AS valor, COUNT(*) AS repeticiones
FROM familias
WHERE linea_id IS NOT NULL AND nombre IS NOT NULL
GROUP BY linea_id, LOWER(nombre)
HAVING COUNT(*) > 1;

SELECT 'Duplicado productos_base(familia_id,codigo)' AS problema, familia_id, LOWER(codigo) AS valor, COUNT(*) AS repeticiones
FROM productos_base
WHERE familia_id IS NOT NULL AND codigo IS NOT NULL
GROUP BY familia_id, LOWER(codigo)
HAVING COUNT(*) > 1;

SELECT 'Duplicado productos_base(familia_id,nombre)' AS problema, familia_id, LOWER(nombre) AS valor, COUNT(*) AS repeticiones
FROM productos_base
WHERE familia_id IS NOT NULL AND nombre IS NOT NULL
GROUP BY familia_id, LOWER(nombre)
HAVING COUNT(*) > 1;

SELECT 'Duplicado niveles(producto_base_id,codigo)' AS problema, producto_base_id, LOWER(codigo) AS valor, COUNT(*) AS repeticiones
FROM niveles
WHERE producto_base_id IS NOT NULL AND codigo IS NOT NULL
GROUP BY producto_base_id, LOWER(codigo)
HAVING COUNT(*) > 1;

-- Las columnas relacionadas por codigo deben compartir esta collation.
SELECT CONCAT('Collation incorrecta: ', table_name, '.', column_name, ' = ', collation_name) AS problema
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
      (table_name = 'proveedor' AND column_name = 'tipo_insumo')
      OR (table_name = 'tipo_insumo_catalogo'
          AND column_name IN ('codigo', 'nombre', 'nombre_normalizado', 'descripcion'))
  )
  AND collation_name <> 'utf8mb4_unicode_ci';
