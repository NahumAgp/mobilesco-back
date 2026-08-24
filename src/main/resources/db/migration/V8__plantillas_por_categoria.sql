delete from modelo_insumo;
delete from modelo_operacion;

alter table insumo modify column tipo_insumo varchar(80);

create table if not exists nivel_insumo (
    id bigint not null auto_increment,
    nivel_id bigint not null,
    insumo_id bigint not null,
    cantidad float(53) not null,
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table if not exists nivel_operacion (
    id bigint not null auto_increment,
    nivel_id bigint not null,
    operacion_id bigint not null,
    cantidad integer not null,
    orden integer not null,
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

alter table nivel_insumo add constraint uk_nivel_insumo unique (nivel_id, insumo_id);
alter table nivel_operacion add constraint uk_nivel_operacion unique (nivel_id, operacion_id);

alter table nivel_insumo add constraint fk_nivel_insumo_nivel foreign key (nivel_id) references niveles (id);
alter table nivel_insumo add constraint fk_nivel_insumo_insumo foreign key (insumo_id) references insumo (id);
alter table nivel_operacion add constraint fk_nivel_operacion_nivel foreign key (nivel_id) references niveles (id);
alter table nivel_operacion add constraint fk_nivel_operacion_operacion foreign key (operacion_id) references operacion (id);
