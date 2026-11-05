ALTER TABLE producto_insumo
    ADD COLUMN costo_unitario_inicial DECIMAL(18,6) NULL;
ALTER TABLE producto_insumo
    ADD COLUMN costo_inicial BIT(1) NOT NULL DEFAULT 0;

ALTER TABLE producto_operacion
    ADD COLUMN tiempo_minutos_inicial DECIMAL(18,6) NULL;
ALTER TABLE producto_operacion
    ADD COLUMN costo_minuto_inicial DECIMAL(18,6) NULL;
ALTER TABLE producto_operacion
    ADD COLUMN costo_inicial BIT(1) NOT NULL DEFAULT 0;
