CREATE TABLE pedido_venta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(30) NOT NULL,
    cotizacion_id BIGINT,
    cliente_id BIGINT,
    estado VARCHAR(30) NOT NULL,
    fecha_pedido DATE NOT NULL,
    fecha_compromiso DATE,
    total DECIMAL(14,2) NOT NULL,
    notas VARCHAR(1000),
    creado_por VARCHAR(150) NOT NULL,
    actualizado_por VARCHAR(150) NOT NULL,
    fecha_registro DATETIME(6) NOT NULL,
    fecha_actualizacion DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_pedido_venta_folio UNIQUE (folio),
    CONSTRAINT uk_pedido_venta_cotizacion UNIQUE (cotizacion_id),
    CONSTRAINT fk_pedido_venta_cotizacion FOREIGN KEY (cotizacion_id) REFERENCES cotizacion (id),
    CONSTRAINT fk_pedido_venta_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pedido_venta_detalle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    sku_snapshot VARCHAR(50) NOT NULL,
    nombre_snapshot VARCHAR(200) NOT NULL,
    cantidad_solicitada DECIMAL(14,3) NOT NULL,
    cantidad_producida DECIMAL(14,3) NOT NULL DEFAULT 0,
    cantidad_reservada DECIMAL(14,3) NOT NULL DEFAULT 0,
    cantidad_entregada DECIMAL(14,3) NOT NULL DEFAULT 0,
    precio_unitario DECIMAL(14,2) NOT NULL,
    importe DECIMAL(14,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_pedido_venta_detalle_producto UNIQUE (pedido_venta_id, producto_id),
    CONSTRAINT fk_pedido_detalle_pedido FOREIGN KEY (pedido_venta_id) REFERENCES pedido_venta (id),
    CONSTRAINT fk_pedido_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventario_producto_terminado (
    id BIGINT NOT NULL AUTO_INCREMENT,
    producto_id BIGINT NOT NULL,
    existencia DECIMAL(14,3) NOT NULL DEFAULT 0,
    reservado DECIMAL(14,3) NOT NULL DEFAULT 0,
    fecha_actualizacion DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_inventario_pt_producto UNIQUE (producto_id),
    CONSTRAINT fk_inventario_pt_producto FOREIGN KEY (producto_id) REFERENCES producto (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE movimiento_producto_terminado (
    id BIGINT NOT NULL AUTO_INCREMENT,
    inventario_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    cantidad DECIMAL(14,3) NOT NULL,
    existencia_anterior DECIMAL(14,3) NOT NULL,
    existencia_nueva DECIMAL(14,3) NOT NULL,
    orden_produccion_id BIGINT,
    pedido_venta_id BIGINT,
    referencia_id BIGINT,
    observaciones VARCHAR(500),
    usuario VARCHAR(150) NOT NULL,
    fecha_registro DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_movimiento_pt_referencia UNIQUE (tipo, referencia_id),
    CONSTRAINT fk_movimiento_pt_inventario FOREIGN KEY (inventario_id) REFERENCES inventario_producto_terminado (id),
    CONSTRAINT fk_movimiento_pt_orden FOREIGN KEY (orden_produccion_id) REFERENCES orden_produccion (id),
    CONSTRAINT fk_movimiento_pt_pedido FOREIGN KEY (pedido_venta_id) REFERENCES pedido_venta (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE orden_produccion ADD COLUMN pedido_venta_id BIGINT;
ALTER TABLE orden_produccion ADD CONSTRAINT fk_op_pedido_venta FOREIGN KEY (pedido_venta_id) REFERENCES pedido_venta (id);
ALTER TABLE orden_produccion_detalle ADD COLUMN pedido_venta_detalle_id BIGINT;
ALTER TABLE orden_produccion_detalle ADD CONSTRAINT fk_opdetalle_pedido_detalle FOREIGN KEY (pedido_venta_detalle_id) REFERENCES pedido_venta_detalle (id);
ALTER TABLE orden_produccion MODIFY COLUMN origen VARCHAR(20) NOT NULL;

CREATE INDEX idx_pedido_venta_estado_fecha ON pedido_venta (estado, fecha_pedido, fecha_compromiso);
CREATE INDEX idx_op_pedido_venta ON orden_produccion (pedido_venta_id);
CREATE INDEX idx_movimiento_pt_fecha ON movimiento_producto_terminado (fecha_registro);

INSERT INTO permissions (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
SELECT 'VIEW_SALES_ORDERS', 'Ver pedidos de venta', 'Ventas', 'Pedidos de venta',
       'Permite abrir y consultar Pedidos de venta.', '/pedidos-venta', 'VIEW', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'VIEW_SALES_ORDERS');

INSERT INTO permissions (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
SELECT 'ACTION_SALES_ORDERS_CREATE', 'Crear pedidos de venta', 'Ventas', 'Pedidos de venta',
       'Crear pedidos de venta desde cotizaciones aceptadas.', '/pedidos-venta', 'ACTION', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ACTION_SALES_ORDERS_CREATE');

INSERT INTO permissions (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
SELECT 'ACTION_SALES_ORDERS_EDIT', 'Confirmar o cancelar pedidos', 'Ventas', 'Pedidos de venta',
       'Confirmar o cancelar pedidos en Pedidos de venta.', '/pedidos-venta', 'ACTION', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ACTION_SALES_ORDERS_EDIT');

INSERT INTO permissions (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
SELECT 'VIEW_FINISHED_GOODS', 'Ver inventario de producto terminado', 'Almacén', 'Producto terminado',
       'Permite consultar existencias y reservas de producto terminado.', '/almacen/producto-terminado', 'VIEW', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'VIEW_FINISHED_GOODS');

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, target.id
FROM role_permissions rp
JOIN permissions source ON source.id = rp.permission_id AND source.code = 'VIEW_QUOTES'
JOIN permissions target ON target.code = 'VIEW_SALES_ORDERS'
WHERE NOT EXISTS (SELECT 1 FROM role_permissions x WHERE x.role_id = rp.role_id AND x.permission_id = target.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, target.id
FROM role_permissions rp
JOIN permissions source ON source.id = rp.permission_id AND source.code = 'ACTION_QUOTES_CREATE'
JOIN permissions target ON target.code = 'ACTION_SALES_ORDERS_CREATE'
WHERE NOT EXISTS (SELECT 1 FROM role_permissions x WHERE x.role_id = rp.role_id AND x.permission_id = target.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, target.id
FROM role_permissions rp
JOIN permissions source ON source.id = rp.permission_id AND source.code = 'ACTION_QUOTES_EDIT'
JOIN permissions target ON target.code = 'ACTION_SALES_ORDERS_EDIT'
WHERE NOT EXISTS (SELECT 1 FROM role_permissions x WHERE x.role_id = rp.role_id AND x.permission_id = target.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, target.id
FROM role_permissions rp
JOIN permissions source ON source.id = rp.permission_id AND source.code = 'VIEW_INVENTORY'
JOIN permissions target ON target.code = 'VIEW_FINISHED_GOODS'
WHERE NOT EXISTS (SELECT 1 FROM role_permissions x WHERE x.role_id = rp.role_id AND x.permission_id = target.id);

INSERT INTO user_permissions (user_id, permission_id)
SELECT DISTINCT up.user_id, target.id
FROM user_permissions up
JOIN permissions source ON source.id = up.permission_id AND source.code = 'VIEW_QUOTES'
JOIN permissions target ON target.code = 'VIEW_SALES_ORDERS'
WHERE NOT EXISTS (SELECT 1 FROM user_permissions x WHERE x.user_id = up.user_id AND x.permission_id = target.id);

INSERT INTO user_permissions (user_id, permission_id)
SELECT DISTINCT up.user_id, target.id
FROM user_permissions up
JOIN permissions source ON source.id = up.permission_id AND source.code = 'ACTION_QUOTES_CREATE'
JOIN permissions target ON target.code = 'ACTION_SALES_ORDERS_CREATE'
WHERE NOT EXISTS (SELECT 1 FROM user_permissions x WHERE x.user_id = up.user_id AND x.permission_id = target.id);

INSERT INTO user_permissions (user_id, permission_id)
SELECT DISTINCT up.user_id, target.id
FROM user_permissions up
JOIN permissions source ON source.id = up.permission_id AND source.code = 'ACTION_QUOTES_EDIT'
JOIN permissions target ON target.code = 'ACTION_SALES_ORDERS_EDIT'
WHERE NOT EXISTS (SELECT 1 FROM user_permissions x WHERE x.user_id = up.user_id AND x.permission_id = target.id);

INSERT INTO user_permissions (user_id, permission_id)
SELECT DISTINCT up.user_id, target.id
FROM user_permissions up
JOIN permissions source ON source.id = up.permission_id AND source.code = 'VIEW_INVENTORY'
JOIN permissions target ON target.code = 'VIEW_FINISHED_GOODS'
WHERE NOT EXISTS (SELECT 1 FROM user_permissions x WHERE x.user_id = up.user_id AND x.permission_id = target.id);
