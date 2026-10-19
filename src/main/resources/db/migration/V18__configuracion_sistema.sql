create table configuracion_sistema (
    id bigint not null,
    nombre_empresa varchar(160) not null,
    razon_social varchar(180),
    rfc varchar(20),
    correo varchar(160),
    telefono varchar(40),
    direccion varchar(255),
    sitio_web varchar(180),
    moneda varchar(3) not null,
    color_primario varchar(7) not null,
    logo_sistema_url varchar(255),
    logo_documentos_url varchar(255),
    logo_catalogo_url varchar(255),
    cobertura_abc_a double not null,
    cobertura_abc_b double not null,
    cobertura_abc_c double not null,
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

insert into configuracion_sistema (
    id, nombre_empresa, moneda, color_primario,
    cobertura_abc_a, cobertura_abc_b, cobertura_abc_c
) values (1, 'Mobilesco', 'MXN', '#324A46', 2.0, 1.5, 1.0);
