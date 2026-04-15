package com.mobilesco.mobilesco_back.dto.Familia;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class FamiliaResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;

    private Long padreId;
    private String padreNombre;

    private Boolean activo;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}