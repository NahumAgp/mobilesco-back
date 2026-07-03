package com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AreaTrabajoResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
