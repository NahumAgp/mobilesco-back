package com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AreaTrabajoUpdateDTO {

    @NotBlank(message = "El codigo es obligatorio")
    @Size(max = 50)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    private String nombre;

    @Size(max = 250)
    private String descripcion;

    private Boolean activo;
}
