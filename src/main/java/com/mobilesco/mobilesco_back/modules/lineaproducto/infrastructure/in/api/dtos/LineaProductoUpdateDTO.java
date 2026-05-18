/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/lineaproducto/infrastructure/in/api/dtos/LineaProductoUpdateDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaProductoUpdateDTO
 * CONTEXTO: DTO de entrada para actualizar linea de producto.
 * NOTAS: Conserva validaciones de nombre y descripcion.
 */
package com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LineaProductoUpdateDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
    
    private Boolean activo;
}
