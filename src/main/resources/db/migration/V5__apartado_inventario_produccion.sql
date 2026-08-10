alter table insumo
    add column stock_apartado double precision not null default 0 after stock_actual;

alter table orden_produccion_insumo
    add column cantidad_apartada decimal(16,4) not null default 0 after cantidad_surtida;
