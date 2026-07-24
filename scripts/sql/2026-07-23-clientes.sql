CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL,
    clasificacion VARCHAR(20) NOT NULL,
    tipo_persona VARCHAR(10) NOT NULL,
    nombre VARCHAR(150) NULL,
    razon_social VARCHAR(180) NULL,
    nombre_comercial VARCHAR(180) NULL,
    rfc VARCHAR(13) NULL,
    contacto_nombre VARCHAR(150) NULL,
    correo VARCHAR(150) NULL,
    telefono VARCHAR(25) NULL,
    whatsapp VARCHAR(25) NULL,
    estado VARCHAR(120) NULL,
    ciudad VARCHAR(120) NULL,
    colonia VARCHAR(120) NULL,
    calle VARCHAR(180) NULL,
    numero_exterior VARCHAR(20) NULL,
    numero_interior VARCHAR(20) NULL,
    codigo_postal VARCHAR(10) NULL,
    dias_credito INT NOT NULL DEFAULT 0,
    limite_credito DECIMAL(14,2) NOT NULL DEFAULT 0,
    notas VARCHAR(1000) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro DATETIME(6) NOT NULL,
    fecha_actualizacion DATETIME(6) NOT NULL,
    CONSTRAINT pk_cliente PRIMARY KEY (id),
    CONSTRAINT uk_cliente_codigo UNIQUE (codigo),
    CONSTRAINT uk_cliente_rfc UNIQUE (rfc),
    CONSTRAINT ck_cliente_dias_credito CHECK (dias_credito >= 0),
    CONSTRAINT ck_cliente_limite_credito CHECK (limite_credito >= 0),
    CONSTRAINT ck_cliente_identidad CHECK (
        nombre IS NOT NULL OR razon_social IS NOT NULL OR nombre_comercial IS NOT NULL
    )
);

CREATE INDEX idx_cliente_clasificacion_activo ON cliente (clasificacion, activo);
CREATE INDEX idx_cliente_nombre ON cliente (nombre);
CREATE INDEX idx_cliente_razon_social ON cliente (razon_social);

-- Evolución futura:
-- ALTER TABLE cotizacion ADD COLUMN cliente_id BIGINT ...;
-- ALTER TABLE venta ADD COLUMN cliente_id BIGINT ...;
-- Las FK se crearán con cada módulo para definir correctamente nulabilidad y política histórica.
