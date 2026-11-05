ALTER TABLE nivel_insumo
    ADD COLUMN costo_unitario_inicial DECIMAL(18,6) NULL;

ALTER TABLE nivel_operacion
    ADD COLUMN material_id BIGINT NULL;
ALTER TABLE nivel_operacion
    ADD COLUMN tiempo_minutos_inicial DECIMAL(18,6) NULL;
ALTER TABLE nivel_operacion
    ADD COLUMN costo_minuto_inicial DECIMAL(18,6) NULL;

CREATE INDEX idx_nivel_operacion_nivel ON nivel_operacion(nivel_id);
ALTER TABLE nivel_operacion DROP INDEX uk_nivel_operacion;
ALTER TABLE nivel_operacion
    ADD CONSTRAINT uk_nivel_operacion_material UNIQUE (nivel_id, material_id, operacion_id);
ALTER TABLE nivel_operacion
    ADD CONSTRAINT fk_nivel_operacion_material FOREIGN KEY (material_id) REFERENCES material(id);
