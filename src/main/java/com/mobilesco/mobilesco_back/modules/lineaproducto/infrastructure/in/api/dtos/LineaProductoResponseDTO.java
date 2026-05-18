/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/lineaproducto/infrastructure/in/api/dtos/LineaProductoResponseDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaProductoResponseDTO
 * CONTEXTO: DTO de salida para respuestas de LineaProducto.
 * NOTAS: Se usa en vistas de listado y detalle.
 */
package com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineaProductoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
