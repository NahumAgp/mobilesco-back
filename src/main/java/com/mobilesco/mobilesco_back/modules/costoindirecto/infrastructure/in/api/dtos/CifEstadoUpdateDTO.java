package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CifEstadoUpdateDTO {
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}
