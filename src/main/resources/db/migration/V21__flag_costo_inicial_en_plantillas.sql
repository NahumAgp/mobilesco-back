ALTER TABLE nivel_insumo
    ADD COLUMN costo_inicial BIT(1) NOT NULL DEFAULT 0;

ALTER TABLE nivel_operacion
    ADD COLUMN costo_inicial BIT(1) NOT NULL DEFAULT 0;

UPDATE nivel_insumo SET costo_inicial=1 WHERE costo_unitario_inicial IS NOT NULL;
UPDATE nivel_operacion SET costo_inicial=1
WHERE tiempo_minutos_inicial IS NOT NULL OR costo_minuto_inicial IS NOT NULL;

UPDATE insumo SET tipo_insumo=NULL WHERE tipo_insumo='COSTOS_2026';
