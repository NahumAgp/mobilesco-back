package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeloOperacionDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String centroTrabajoNombre;
    private Integer orden;
    private Boolean activo;
}
