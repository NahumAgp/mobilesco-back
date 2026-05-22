package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitacionUsuarioResponseDTO {

    private Long id;
    private String email;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;
    private String puesto;
    private String rol;
    private String token;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;
    private String createdBy;
    private String usedBy;
}
