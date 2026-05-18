/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/material/infrastructure/in/api/dtos/MaterialResponseDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: MaterialResponseDTO
 * CONTEXTO: DTO de salida para respuestas del modulo Material.
 * NOTAS: Incluye metadatos de auditoria para UI.
 */
package com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos;

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
