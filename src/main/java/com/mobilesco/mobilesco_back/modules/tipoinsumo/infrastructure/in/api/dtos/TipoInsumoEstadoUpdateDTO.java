package com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoInsumoEstadoUpdateDTO {

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}
