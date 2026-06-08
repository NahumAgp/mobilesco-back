package com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoInsumoUpdateDTO {

    @NotBlank(message = "El nombre del tipo de insumo es obligatorio")
    private String nombre;

    private String descripcion;
}
