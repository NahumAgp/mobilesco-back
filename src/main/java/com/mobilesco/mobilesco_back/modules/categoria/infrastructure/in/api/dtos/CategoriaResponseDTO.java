/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/categoria/infrastructure/in/api/dtos/CategoriaResponseDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: CategoriaResponseDTO
 * CONTEXTO: DTO de salida para respuestas de Categoria.
 * NOTAS: Se usa en listados y detalle de categoria.
 */
package com.mobilesco.mobilesco_back.modules.categoria.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
