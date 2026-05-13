package com.mobilesco.mobilesco_back.dto.material;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MaterialResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
