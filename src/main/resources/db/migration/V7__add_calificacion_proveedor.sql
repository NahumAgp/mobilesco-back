alter table proveedor
    add column calificacion_proveedor decimal(5,2);

alter table proveedor
    add constraint chk_proveedor_calificacion
    check (calificacion_proveedor between 0.00 and 100.00);
