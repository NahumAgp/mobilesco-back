package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermisoResponseDTO {
    private Long id;
    private String code;
    private String nombre;
    private String modulo;
    private String vista;
    private String descripcion;
    private String ruta;
    private String tipo;
    private String vistaRequerida;
    private boolean activo;
}
