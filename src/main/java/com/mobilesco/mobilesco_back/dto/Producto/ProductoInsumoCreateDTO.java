package com.mobilesco.mobilesco_back.dto.Producto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductoInsumoCreateDTO {
    
    @NotNull(message = "El insumo es obligatorio")
    private Long insumoId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Double cantidad;
    
    @Positive(message = "El desperdicio debe ser mayor o igual a 0")
    private Double desperdicioPorcentaje;
    
    private String observaciones;
}