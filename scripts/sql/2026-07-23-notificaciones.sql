CREATE TABLE IF NOT EXISTS notificacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    destinatario_usuario_id BIGINT NOT NULL,
    tipo VARCHAR(25) NOT NULL,
    titulo VARCHAR(180) NOT NULL,
    mensaje VARCHAR(1000) NOT NULL,
    modulo VARCHAR(80) NULL,
    entidad_tipo VARCHAR(80) NULL,
    entidad_id BIGINT NULL,
    ruta VARCHAR(500) NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_lectura DATETIME(6) NULL,
    CONSTRAINT pk_notificacion PRIMARY KEY (id),
    CONSTRAINT fk_notificacion_destinatario
        FOREIGN KEY (destinatario_usuario_id) REFERENCES users(id)
);

CREATE INDEX idx_notificacion_usuario_lectura_fecha
    ON notificacion (destinatario_usuario_id, leida, fecha_creacion);
CREATE INDEX idx_notificacion_entidad
    ON notificacion (entidad_tipo, entidad_id);

-- Cada fila representa el estado de lectura de un destinatario.
-- Correo, SMS, WhatsApp y push quedan fuera de esta etapa.
