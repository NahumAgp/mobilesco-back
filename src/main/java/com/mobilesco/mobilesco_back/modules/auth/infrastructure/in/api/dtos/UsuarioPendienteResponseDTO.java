package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioPendienteResponseDTO {

    private Long idUsuario;
    private String correo;
    private String puesto;
    private String rol;
    private String estadoCuenta;
    private String aprobacionDirector;
    private String aprobacionDev;
    private LocalDateTime aprobacionDirectorAt;
    private LocalDateTime aprobacionDevAt;
    private LocalDateTime fechaRegistro;
}
