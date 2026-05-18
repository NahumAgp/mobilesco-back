/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/dtos/ProductoInsumoCreateDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoInsumoCreateDTO
 * CONTEXTO: DTO de entrada para asociar insumos a productos.
 * NOTAS: Se usa en endpoints BOM de productos.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductoInsumoCreateDTO {
    
    @NotNull(message = "El insumo es obligatorio")
    private Long insumoId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Double cantidad;
    
    @PositiveOrZero(message = "El desperdicio debe ser mayor o igual a 0")
    private Double desperdicioPorcentaje;
    
    private String observaciones;
}
