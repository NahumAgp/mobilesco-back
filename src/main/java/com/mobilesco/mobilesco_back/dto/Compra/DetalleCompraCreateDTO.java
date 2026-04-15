package com.mobilesco.mobilesco_back.dto.Compra;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DetalleCompraCreateDTO {
    
    @NotNull(message = "El insumo es obligatorio")
    private Long insumoId;
    
    @NotNull(message = "La unidad de compra es obligatoria")
    private Long unidadCompraId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Double cantidad;
    
    @NotNull(message = "El factor de conversión es obligatorio")
    @Positive(message = "El factor de conversión debe ser mayor a 0")
    private Double factorConversion;
    
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a 0")
    private Double precioUnitario;
    
    private Double cantidadRecibida;
    
    private Double subtotal;
    
    private String observaciones;
}