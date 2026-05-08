package com.mobilesco.mobilesco_back.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitacionUsuarioCreateDTO {

    @Email(message = "El correo no tiene un formato valido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    private String apellidoMaterno;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "El puesto es obligatorio")
    private String puesto;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;
}
