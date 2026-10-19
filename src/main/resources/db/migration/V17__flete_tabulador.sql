create table flete_tabulador (
    id bigint not null auto_increment,
    hasta_km decimal(10,2) not null,
    precio decimal(14,2) not null,
    primary key (id),
    constraint uk_flete_tabulador_hasta_km unique (hasta_km)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
