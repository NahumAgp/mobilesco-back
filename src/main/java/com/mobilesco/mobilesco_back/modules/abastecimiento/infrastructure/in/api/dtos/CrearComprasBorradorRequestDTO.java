package com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CrearComprasBorradorRequestDTO {
    @Valid
    @NotEmpty(message = "Debe seleccionar al menos una sugerencia")
    private List<SeleccionSugerenciaDTO> sugerencias;
}
