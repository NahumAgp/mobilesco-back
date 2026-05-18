/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/lineaproducto/infrastructure/in/api/dtos/LineaProductoCreateDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaProductoCreateDTO
 * CONTEXTO: DTO de entrada para crear una linea de producto.
 * NOTAS: Mantiene validaciones sobre campos editables.
 */
package com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LineaProductoCreateDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
}
