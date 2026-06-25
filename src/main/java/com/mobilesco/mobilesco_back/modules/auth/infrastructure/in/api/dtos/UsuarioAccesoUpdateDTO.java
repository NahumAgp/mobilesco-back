package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioAccesoUpdateDTO {
    @Size(min = 1)
    private List<String> roles;

    private List<String> permisosDirectos;
    private Boolean enabled;
    private Boolean locked;
}
