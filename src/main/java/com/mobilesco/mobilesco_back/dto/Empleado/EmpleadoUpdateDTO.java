package com.mobilesco.mobilesco_back.dto.Empleado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpleadoUpdateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    private String apellidoMaterno;

    @Pattern(
        regexp = "^[0-9]{10}$",
        message = "El teléfono debe contener exactamente 10 dígitos numéricos"
    )
    private String telefono;

    private String fechaNacimiento;

    private String email;
    private String password;

}
