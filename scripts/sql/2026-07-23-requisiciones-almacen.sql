CREATE TABLE IF NOT EXISTS requisicion_almacen (
    id BIGINT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(30) NOT NULL,
    solicitante_usuario_id BIGINT NOT NULL,
    solicitante_nombre VARCHAR(180) NOT NULL,
    destinatario_rol VARCHAR(50) NOT NULL DEFAULT 'SUBDIRECCION_ADMINISTRATIVA',
    estado VARCHAR(20) NOT NULL,
    observaciones VARCHAR(1000) NULL,
    comentario_resolucion VARCHAR(1000) NULL,
    resuelto_por VARCHAR(190) NULL,
    fecha_envio DATETIME(6) NOT NULL,
    fecha_resolucion DATETIME(6) NULL,
    fecha_actualizacion DATETIME(6) NOT NULL,
    CONSTRAINT pk_requisicion_almacen PRIMARY KEY (id),
    CONSTRAINT uk_requisicion_almacen_folio UNIQUE (folio),
    CONSTRAINT fk_requisicion_solicitante
        FOREIGN KEY (solicitante_usuario_id) REFERENCES users(id)
);

CREATE INDEX idx_requisicion_estado_fecha
    ON requisicion_almacen (estado, fecha_envio);
CREATE INDEX idx_requisicion_solicitante_fecha
    ON requisicion_almacen (solicitante_usuario_id, fecha_envio);

CREATE TABLE IF NOT EXISTS requisicion_almacen_detalle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    requisicion_id BIGINT NOT NULL,
    insumo_id BIGINT NOT NULL,
    insumo_codigo VARCHAR(150) NOT NULL,
    insumo_nombre VARCHAR(150) NOT NULL,
    unidad_simbolo VARCHAR(30) NULL,
    cantidad_solicitada DOUBLE NOT NULL,
    stock_actual_snapshot DOUBLE NOT NULL,
    stock_minimo_snapshot DOUBLE NULL,
    origen_sugerencia BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones VARCHAR(500) NULL,
    CONSTRAINT pk_requisicion_almacen_detalle PRIMARY KEY (id),
    CONSTRAINT uk_requisicion_almacen_insumo UNIQUE (requisicion_id, insumo_id),
    CONSTRAINT fk_requisicion_detalle_requisicion
        FOREIGN KEY (requisicion_id) REFERENCES requisicion_almacen(id),
    CONSTRAINT fk_requisicion_detalle_insumo
        FOREIGN KEY (insumo_id) REFERENCES insumo(id),
    CONSTRAINT ck_requisicion_cantidad CHECK (cantidad_solicitada > 0)
);

CREATE INDEX idx_requisicion_detalle_insumo
    ON requisicion_almacen_detalle (insumo_id);

-- Este módulo no modifica stock ni Kardex.
-- Una futura compra podrá referenciar requisicion_almacen.id desde su propia migración.
