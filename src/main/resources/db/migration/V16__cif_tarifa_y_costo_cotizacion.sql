create table cif_tarifa_version (
    id bigint not null auto_increment,
    anio integer not null,
    mes integer not null,
    configuracion_cif_id bigint not null,
    total_mensual decimal(18,6) not null,
    minutos_productivos_mes decimal(18,6) not null,
    tasa_minuto decimal(18,8) not null,
    fecha_registro datetime(6) not null,
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

alter table cotizacion_detalle add column cif_tarifa_version_id bigint null;
alter table cotizacion_detalle add column costo_insumos decimal(14,2) null;
alter table cotizacion_detalle add column costo_operaciones decimal(14,2) null;
alter table cotizacion_detalle add column costo_cif decimal(14,2) null;

alter table cotizacion_detalle
    add constraint fk_cotdetalle_cif_tarifa_version
    foreign key (cif_tarifa_version_id) references cif_tarifa_version (id);

create index idx_cotdetalle_cif_tarifa_version on cotizacion_detalle (cif_tarifa_version_id);

alter table insumo add column fecha_costo_cotizacion datetime(6) null;
-- Periodo de transición: los costos existentes con valor conservan 90 días de vigencia.
update insumo set fecha_costo_cotizacion = current_timestamp
where costo_cotizar is not null and costo_cotizar > 0 and conjunto = false;

alter table insumo modify column costo_cotizar decimal(18,6) null;
alter table operacion modify column costo_hora decimal(18,6) null;
alter table operacion modify column costo_minuto decimal(18,6) null;
alter table producto_operacion modify column importe_actividad decimal(18,6) null;
alter table costo_indirecto modify column monto_mensual decimal(18,6) null;
alter table centro_trabajo modify column costo_hora decimal(18,6) not null;
alter table centro_trabajo modify column costo_minuto decimal(18,6) null;
alter table detalle_compra modify column precio_unitario decimal(18,6) not null;
alter table detalle_compra modify column subtotal decimal(18,6) null;
alter table kardex modify column costo_unitario decimal(18,6) not null;
alter table kardex modify column costo_total decimal(18,6) not null;
alter table detalle_salida_insumo modify column costo_unitario decimal(18,6) not null;
alter table detalle_salida_insumo modify column costo_total decimal(18,6) not null;

-- La aplicación siempre ha prorrateado todos los conceptos por minuto MOD.
-- Los otros valores y parámetros nunca participaban en el cálculo.
create table cif_configuracion_legacy (
    costo_indirecto_id bigint not null,
    base_distribucion varchar(40) not null,
    porcentaje_asignado decimal(18,6) null,
    tasa_variable decimal(18,6) null,
    fecha_migracion datetime(6) not null,
    primary key (costo_indirecto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

insert into cif_configuracion_legacy
    (costo_indirecto_id, base_distribucion, porcentaje_asignado, tasa_variable, fecha_migracion)
select id, base_distribucion, porcentaje_asignado, tasa_variable, current_timestamp
from costo_indirecto
where base_distribucion <> 'HORAS_MOD' or porcentaje_asignado is not null or tasa_variable is not null;

update costo_indirecto
set base_distribucion = 'HORAS_MOD', porcentaje_asignado = null, tasa_variable = null
where base_distribucion <> 'HORAS_MOD' or porcentaje_asignado is not null or tasa_variable is not null;
