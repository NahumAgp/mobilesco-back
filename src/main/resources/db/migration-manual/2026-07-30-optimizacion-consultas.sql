-- Indices para las rutas paginadas de insumos y su enriquecimiento por lote.
-- Esta migracion se aplica una sola vez en MySQL. Los indices de llaves foraneas
-- que ya existan con otro nombre pueden omitirse tras verificar SHOW INDEX.

CREATE INDEX idx_insumo_activo_nombre_id
  ON insumo (activo, nombre, id);

CREATE INDEX idx_insumo_activo_stock
  ON insumo (activo, stock_actual, stock_minimo);

CREATE INDEX idx_detalle_compra_insumo_compra
  ON detalle_compra (insumo_id, compra_id, id);

CREATE INDEX idx_kardex_insumo_tipo
  ON kardex (insumo_id, tipo);

CREATE INDEX idx_producto_insumo_insumo
  ON producto_insumo (insumo_id);

CREATE INDEX idx_detalle_salida_insumo_insumo
  ON detalle_salida_insumo (insumo_id);

CREATE INDEX idx_role_permissions_role_permission
  ON role_permissions (role_id, permission_id);

CREATE INDEX idx_user_roles_user_role
  ON user_roles (user_id, role_id);
