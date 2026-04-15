package com.mobilesco.mobilesco_back.dto.Empleado;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpleadoEstatusDTO {

    @NotBlank(message = "El estatus es obligatorio")
    private String estatus;

}