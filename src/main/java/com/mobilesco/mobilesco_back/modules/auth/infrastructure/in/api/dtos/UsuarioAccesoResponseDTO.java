package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioAccesoResponseDTO {
    private Long idUsuario;
    private String correo;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String estadoCuenta;
    private boolean enabled;
    private boolean locked;
    private LocalDateTime lastLoginAt;
    private List<String> roles;
    private List<String> permisosDirectos;
    private List<String> permisosEfectivos;
}
