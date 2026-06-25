package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolCreateDTO {
    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 160)
    private String descripcion;

    private List<String> permisos;
}
