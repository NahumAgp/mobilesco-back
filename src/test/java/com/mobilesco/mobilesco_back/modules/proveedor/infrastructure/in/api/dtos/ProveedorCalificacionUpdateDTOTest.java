package com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class ProveedorCalificacionUpdateDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "87.45", "100.00"})
    void aceptaCalificacionesDentroDelRango(String valor) {
        ProveedorCalificacionUpdateDTO dto = dtoConCalificacion(valor);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "100.01"})
    void rechazaCalificacionesFueraDelRango(String valor) {
        ProveedorCalificacionUpdateDTO dto = dtoConCalificacion(valor);

        assertThat(validator.validate(dto))
                .anySatisfy(violacion ->
                        assertThat(violacion.getPropertyPath().toString())
                                .isEqualTo("calificacionProveedor"));
    }

    @Test
    void permiteQuitarLaCalificacionParaVolverASinCalificar() {
        ProveedorCalificacionUpdateDTO dto = new ProveedorCalificacionUpdateDTO();
        dto.setCalificacionProveedor(null);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void rechazaUnaSolicitudQueOmiteElCampo() {
        ProveedorCalificacionUpdateDTO dto = new ProveedorCalificacionUpdateDTO();

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    private ProveedorCalificacionUpdateDTO dtoConCalificacion(String valor) {
        ProveedorCalificacionUpdateDTO dto = new ProveedorCalificacionUpdateDTO();
        dto.setCalificacionProveedor(new BigDecimal(valor));
        return dto;
    }
}
