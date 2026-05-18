/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/dtos/ProductoInsumoListaDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoInsumoListaDTO
 * CONTEXTO: DTO contenedor para carga masiva de insumos.
 * NOTAS: Facilita operaciones bulk en BOM.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ProductoInsumoListaDTO {
    
    @NotEmpty(message = "Debe incluir al menos un insumo")
    private List<ProductoInsumoCreateDTO> insumos;
}
