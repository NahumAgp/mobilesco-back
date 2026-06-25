package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioCreateDTO {
    @NotBlank
    @Email
    @Size(max = 190)
    private String email;

    @NotBlank
    @Size(min = 8, max = 120)
    private String password;

    @Size(min = 1)
    private List<String> roles;

    private List<String> permisosDirectos;
}
