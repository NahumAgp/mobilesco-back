-- Plantilla productiva por modelo: insumos y operaciones heredables.
-- Ejecutar antes de desplegar el backend en produccion (ddl-auto=validate).

CREATE TABLE IF NOT EXISTS modelo_insumo (
    modelo_id BIGINT NOT NULL,
    insumo_id BIGINT NOT NULL,
    CONSTRAINT pk_modelo_insumo PRIMARY KEY (modelo_id, insumo_id),
    CONSTRAINT fk_modelo_insumo_modelo
        FOREIGN KEY (modelo_id) REFERENCES productos_base(id),
    CONSTRAINT fk_modelo_insumo_insumo
        FOREIGN KEY (insumo_id) REFERENCES insumos(id)
);

CREATE INDEX idx_modelo_insumo_insumo ON modelo_insumo(insumo_id);

CREATE TABLE IF NOT EXISTS modelo_operacion (
    modelo_id BIGINT NOT NULL,
    operacion_id BIGINT NOT NULL,
    orden INT NOT NULL,
    CONSTRAINT pk_modelo_operacion PRIMARY KEY (modelo_id, operacion_id),
    CONSTRAINT uk_modelo_operacion_orden UNIQUE (modelo_id, orden),
    CONSTRAINT fk_modelo_operacion_modelo
        FOREIGN KEY (modelo_id) REFERENCES productos_base(id),
    CONSTRAINT fk_modelo_operacion_operacion
        FOREIGN KEY (operacion_id) REFERENCES operaciones(id)
);

CREATE INDEX idx_modelo_operacion_operacion ON modelo_operacion(operacion_id);

ALTER TABLE producto_insumo MODIFY COLUMN cantidad DOUBLE NULL;
ALTER TABLE producto_operacion MODIFY COLUMN cantidad INT NULL;
