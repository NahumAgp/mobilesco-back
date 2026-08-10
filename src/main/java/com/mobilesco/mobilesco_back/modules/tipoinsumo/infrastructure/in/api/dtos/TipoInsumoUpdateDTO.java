package com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoInsumoUpdateDTO {

    @NotBlank(message = "El codigo del tipo de insumo es obligatorio")
    @Size(max = 3, message = "El codigo debe tener maximo 3 caracteres")
    @Pattern(regexp = "[A-Za-z0-9]+", message = "El codigo solo puede contener letras y numeros")
    private String codigo;

    @NotBlank(message = "El nombre del tipo de insumo es obligatorio")
    private String nombre;

    private String descripcion;
}
