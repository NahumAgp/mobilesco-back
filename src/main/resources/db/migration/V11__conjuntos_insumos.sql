ALTER TABLE insumo ADD COLUMN conjunto BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS insumo_componente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conjunto_id BIGINT NOT NULL,
    insumo_id BIGINT NOT NULL,
    cantidad DOUBLE PRECISION NOT NULL CHECK (cantidad > 0),
    CONSTRAINT uk_insumo_componente UNIQUE (conjunto_id, insumo_id),
    CONSTRAINT ck_componente_distinto CHECK (conjunto_id <> insumo_id),
    CONSTRAINT fk_componente_conjunto FOREIGN KEY (conjunto_id) REFERENCES insumo(id),
    CONSTRAINT fk_componente_insumo FOREIGN KEY (insumo_id) REFERENCES insumo(id)
);
CREATE INDEX idx_insumo_componente_insumo ON insumo_componente(insumo_id);
