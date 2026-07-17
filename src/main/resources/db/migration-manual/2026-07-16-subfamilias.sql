CREATE TABLE IF NOT EXISTS subfamilias (
  id BIGINT NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(10) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255) NULL,
  activo BIT(1) NULL DEFAULT b'1',
  created_at DATETIME(6) NULL,
  familia_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_subfamilia_familia_codigo UNIQUE (familia_id, codigo),
  CONSTRAINT uk_subfamilia_familia_nombre UNIQUE (familia_id, nombre),
  CONSTRAINT fk_subfamilia_familia FOREIGN KEY (familia_id) REFERENCES familias (id)
);

ALTER TABLE productos_base
  ADD COLUMN subfamilia_id BIGINT NULL;

ALTER TABLE productos_base
  ADD CONSTRAINT fk_modelo_subfamilia FOREIGN KEY (subfamilia_id) REFERENCES subfamilias (id);

CREATE INDEX idx_productos_base_subfamilia_id ON productos_base (subfamilia_id);
