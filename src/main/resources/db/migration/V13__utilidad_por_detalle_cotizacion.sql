ALTER TABLE cotizacion_detalle
    ADD COLUMN utilidad_porcentaje DECIMAL(7,2) NULL AFTER cantidad;

ALTER TABLE cotizacion
    MODIFY COLUMN margen_porcentaje DECIMAL(7,2) NULL;
