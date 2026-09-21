alter table nivel_insumo
    add column material_id bigint null;

create index idx_nivel_insumo_nivel on nivel_insumo (nivel_id);

alter table nivel_insumo
    drop index uk_nivel_insumo;

alter table nivel_insumo
    add constraint uk_nivel_insumo_material unique (nivel_id, material_id, insumo_id);

alter table nivel_insumo
    add constraint fk_nivel_insumo_material foreign key (material_id) references material (id);
