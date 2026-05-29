package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InsumoEstadoUpdateDTO {
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}
