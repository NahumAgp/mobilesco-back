package com.mobilesco.mobilesco_back.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroInvitacionResponseDTO {

    private Long idUsuario;
    private String correo;
    private String mensaje;
}
