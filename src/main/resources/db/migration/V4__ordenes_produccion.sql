create table orden_produccion (
    id bigint not null auto_increment,
    folio varchar(30) not null,
    origen enum ('MANUAL','COTIZACION') not null,
    estado enum ('BORRADOR','LIBERADA','EN_PROCESO','TERMINADA','CANCELADA') not null,
    cotizacion_id bigint,
    cliente_id bigint,
    fecha_inicio_programada date,
    fecha_compromiso date,
    observaciones varchar(1000),
    motivo_cancelacion varchar(1000),
    creado_por varchar(150) not null,
    actualizado_por varchar(150) not null,
    fecha_registro datetime(6) not null,
    fecha_actualizacion datetime(6) not null,
    primary key (id),
    constraint uk_orden_produccion_folio unique (folio),
    constraint uk_orden_produccion_cotizacion unique (cotizacion_id),
    constraint fk_op_cotizacion foreign key (cotizacion_id) references cotizacion (id),
    constraint fk_op_cliente foreign key (cliente_id) references cliente (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table orden_produccion_detalle (
    id bigint not null auto_increment,
    orden_produccion_id bigint not null,
    producto_id bigint not null,
    sku_snapshot varchar(50) not null,
    nombre_snapshot varchar(200) not null,
    cantidad_planeada decimal(14,3) not null,
    cantidad_terminada decimal(14,3) not null default 0,
    primary key (id),
    constraint uk_op_detalle_producto unique (orden_produccion_id, producto_id),
    constraint fk_opdetalle_orden foreign key (orden_produccion_id) references orden_produccion (id),
    constraint fk_opdetalle_producto foreign key (producto_id) references producto (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table orden_produccion_insumo (
    id bigint not null auto_increment,
    orden_produccion_id bigint not null,
    insumo_id bigint not null,
    codigo_snapshot varchar(150) not null,
    nombre_snapshot varchar(150) not null,
    unidad_snapshot varchar(20) not null,
    cantidad_requerida decimal(16,4) not null,
    cantidad_surtida decimal(16,4) not null default 0,
    primary key (id),
    constraint uk_op_insumo unique (orden_produccion_id, insumo_id),
    constraint fk_opinsumo_orden foreign key (orden_produccion_id) references orden_produccion (id),
    constraint fk_opinsumo_insumo foreign key (insumo_id) references insumo (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table orden_produccion_operacion (
    id bigint not null auto_increment,
    orden_detalle_id bigint not null,
    operacion_id bigint not null,
    codigo_snapshot varchar(50) not null,
    nombre_snapshot varchar(100) not null,
    centro_trabajo_snapshot varchar(150) not null,
    secuencia integer not null,
    repeticiones_planeadas integer not null,
    tiempo_planeado decimal(16,3) not null,
    estado enum ('PENDIENTE','EN_PROCESO','TERMINADA') not null,
    fecha_inicio datetime(6),
    fecha_fin datetime(6),
    primary key (id),
    constraint fk_opoperacion_detalle foreign key (orden_detalle_id) references orden_produccion_detalle (id),
    constraint fk_opoperacion_operacion foreign key (operacion_id) references operacion (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table orden_produccion_avance (
    id bigint not null auto_increment,
    orden_detalle_id bigint not null,
    cantidad decimal(14,3) not null,
    observaciones varchar(500),
    usuario varchar(150) not null,
    fecha_registro datetime(6) not null,
    primary key (id),
    constraint fk_opavance_detalle foreign key (orden_detalle_id) references orden_produccion_detalle (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

alter table salida_insumo add column orden_produccion_id bigint;
alter table salida_insumo add constraint fk_salida_orden_produccion foreign key (orden_produccion_id) references orden_produccion (id);
create index idx_op_estado_fechas on orden_produccion (estado, fecha_inicio_programada, fecha_compromiso);
create index idx_op_origen on orden_produccion (origen);
create index idx_salida_orden_produccion on salida_insumo (orden_produccion_id);
