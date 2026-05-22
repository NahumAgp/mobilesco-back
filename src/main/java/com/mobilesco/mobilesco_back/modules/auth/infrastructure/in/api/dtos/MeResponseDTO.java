package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeResponseDTO {

    private Long idUsuario;
    private String correo;
    private List<String> roles;

    private Long idEmpleado;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;
    private String fotoUrl;

}