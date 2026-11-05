package com.mobilesco.mobilesco_back.modules.modelo.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class CostoInicialModeloServiceTest {

    @Test
    void devuelveDesgloseYOperacionesDeLaFichaDelModelo() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:costos_modelo;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE categoria (id BIGINT PRIMARY KEY, nombre VARCHAR(100))");
        jdbc.execute("CREATE TABLE material (id BIGINT PRIMARY KEY, codigo VARCHAR(10), nombre VARCHAR(100))");
        jdbc.execute("CREATE TABLE modelo_material (modelo_id BIGINT, material_id BIGINT)");
        jdbc.execute("CREATE TABLE niveles (id BIGINT PRIMARY KEY, producto_base_id BIGINT, categoria_id BIGINT, costo_inicial_pendiente BOOLEAN)");
        jdbc.execute("CREATE TABLE costo_fuente_hoja (id BIGINT PRIMARY KEY, codigo_fuente VARCHAR(100), archivo VARCHAR(255), hoja VARCHAR(100))");
        jdbc.execute("""
                CREATE TABLE costo_referencia_catalogo (
                  fuente_hoja_id BIGINT PRIMARY KEY, nivel_id BIGINT,
                  materia_prima DECIMAL(18,6), mano_obra DECIMAL(18,6), costo_primo DECIMAL(18,6),
                  indirectos DECIMAL(18,6), costo_fabricacion DECIMAL(18,6), minutos_elaboracion DECIMAL(18,6),
                  precio_base_sin_iva DECIMAL(18,6), precio_menudeo_sin_iva DECIMAL(18,6),
                  precio_foraneo_sin_iva DECIMAL(18,6), precio_base_con_iva DECIMAL(18,6),
                  precio_menudeo_con_iva DECIMAL(18,6), precio_foraneo_con_iva DECIMAL(18,6))
                """);
        jdbc.execute("""
                CREATE TABLE costo_fuente_renglon (
                  id BIGINT PRIMARY KEY, fuente_hoja_id BIGINT, fila INT, tipo VARCHAR(25),
                  descripcion VARCHAR(500), unidad VARCHAR(50), cantidad DECIMAL(18,6),
                  precio_unitario DECIMAL(18,6), importe DECIMAL(18,6), estado VARCHAR(20),
                  motivo VARCHAR(500), insumo_id BIGINT, operacion_id BIGINT)
                """);
        jdbc.execute("INSERT INTO categoria VALUES (1, 'Primaria')");
        jdbc.execute("INSERT INTO material VALUES (7, 'T', 'Triplay de pino')");
        jdbc.execute("INSERT INTO modelo_material VALUES (10, 7)");
        jdbc.execute("INSERT INTO niveles VALUES (2, 10, 1, TRUE)");
        jdbc.execute("INSERT INTO costo_fuente_hoja VALUES (3, 'MTN01', 'MESAS.xlsx', 'MTN01')");
        jdbc.execute("""
                INSERT INTO costo_referencia_catalogo
                (fuente_hoja_id,nivel_id,materia_prima,mano_obra,costo_primo,indirectos,costo_fabricacion,
                 minutos_elaboracion,precio_base_sin_iva,precio_base_con_iva)
                VALUES (3,2,100,20,120,10,130,12,200,232)
                """);
        jdbc.execute("INSERT INTO costo_fuente_renglon VALUES (4,3,14,'MATERIAL','Tubo','M',2,10,20,'PENDIENTE','Por vincular',NULL,NULL)");
        jdbc.execute("INSERT INTO costo_fuente_renglon VALUES (5,3,33,'OPERACION','Corte','min',3,1,3,'PENDIENTE','Por vincular',NULL,NULL)");

        var fichas = new CostoInicialModeloService(jdbc).obtenerPorModelo(10L);

        assertThat(fichas).hasSize(1);
        var ficha = fichas.getFirst();
        assertThat(ficha.codigoFuente()).isEqualTo("MTN01");
        assertThat(ficha.categoria()).isEqualTo("Primaria");
        assertThat(ficha.materialId()).isEqualTo(7L);
        assertThat(ficha.costoFabricacion()).isEqualByComparingTo(new BigDecimal("130"));
        assertThat(ficha.materiales()).hasSize(1);
        assertThat(ficha.materiales().getFirst().descripcion()).isEqualTo("Tubo");
        assertThat(ficha.operaciones()).hasSize(1);
        assertThat(ficha.operaciones().getFirst().descripcion()).isEqualTo("Corte");
        assertThat(new CostoInicialModeloService(jdbc).obtenerPorModelo(99L)).isEmpty();
    }
}
