package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolResponseDTO {
    private Long id;
    private String name;
    private String descripcion;
    private boolean sistema;
    private List<String> permisos;
}
