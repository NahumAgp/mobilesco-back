ALTER TABLE productos_base
    ADD COLUMN costo_inicial_pendiente bit(1) NOT NULL DEFAULT 0;

ALTER TABLE niveles
    ADD COLUMN costo_inicial_pendiente bit(1) NOT NULL DEFAULT 0;

CREATE TABLE costo_fuente_hoja (
    id bigint NOT NULL AUTO_INCREMENT,
    archivo varchar(255) NOT NULL,
    archivo_sha256 char(64) NOT NULL,
    hoja varchar(100) NOT NULL,
    celdas_json longtext NOT NULL,
    codigo_fuente varchar(100),
    producto_id bigint,
    estado varchar(20) NOT NULL,
    motivo varchar(500),
    fecha_carga datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_costo_fuente_hoja (archivo_sha256, hoja),
    KEY ix_costo_fuente_producto (producto_id),
    CONSTRAINT fk_costo_fuente_producto FOREIGN KEY (producto_id) REFERENCES producto (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE costo_referencia_catalogo (
    fuente_hoja_id bigint NOT NULL,
    producto_id bigint,
    nivel_id bigint,
    materia_prima decimal(18,6),
    mano_obra decimal(18,6),
    costo_primo decimal(18,6),
    indirectos decimal(18,6),
    costo_fabricacion decimal(18,6),
    minutos_elaboracion decimal(18,6),
    precio_base_sin_iva decimal(18,6),
    precio_menudeo_sin_iva decimal(18,6),
    precio_foraneo_sin_iva decimal(18,6),
    precio_base_con_iva decimal(18,6),
    precio_menudeo_con_iva decimal(18,6),
    precio_foraneo_con_iva decimal(18,6),
    PRIMARY KEY (fuente_hoja_id),
    KEY ix_costo_referencia_producto (producto_id),
    KEY ix_costo_referencia_nivel (nivel_id),
    CONSTRAINT fk_costo_referencia_fuente FOREIGN KEY (fuente_hoja_id) REFERENCES costo_fuente_hoja (id),
    CONSTRAINT fk_costo_referencia_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_costo_referencia_nivel FOREIGN KEY (nivel_id) REFERENCES niveles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE costo_fuente_renglon (
    id bigint NOT NULL AUTO_INCREMENT,
    fuente_hoja_id bigint NOT NULL,
    fila int NOT NULL,
    tipo varchar(25) NOT NULL,
    descripcion varchar(500) NOT NULL,
    unidad varchar(50),
    cantidad decimal(18,6),
    precio_unitario decimal(18,6),
    importe decimal(18,6),
    nivel_id bigint,
    insumo_id bigint,
    operacion_id bigint,
    estado varchar(20) NOT NULL,
    motivo varchar(500),
    PRIMARY KEY (id),
    UNIQUE KEY uk_costo_fuente_renglon (fuente_hoja_id, fila, tipo),
    KEY ix_costo_renglon_nivel (nivel_id),
    CONSTRAINT fk_costo_renglon_fuente FOREIGN KEY (fuente_hoja_id) REFERENCES costo_fuente_hoja (id),
    CONSTRAINT fk_costo_renglon_nivel FOREIGN KEY (nivel_id) REFERENCES niveles (id),
    CONSTRAINT fk_costo_renglon_insumo FOREIGN KEY (insumo_id) REFERENCES insumo (id),
    CONSTRAINT fk_costo_renglon_operacion FOREIGN KEY (operacion_id) REFERENCES operacion (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE costo_importacion_cambio (
    id bigint NOT NULL AUTO_INCREMENT,
    fuente_hoja_id bigint NOT NULL,
    celda varchar(12),
    entidad varchar(40) NOT NULL,
    entidad_id bigint NOT NULL,
    campo varchar(80) NOT NULL,
    valor_anterior decimal(18,6),
    valor_nuevo decimal(18,6),
    fecha_cambio datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_costo_cambio_entidad (entidad, entidad_id),
    CONSTRAINT fk_costo_cambio_fuente FOREIGN KEY (fuente_hoja_id) REFERENCES costo_fuente_hoja (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
