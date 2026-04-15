package com.mobilesco.mobilesco_back.dto.Compra;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DetalleCompraUpdateDTO {
    
    private Long unidadCompraId;
    
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Double cantidad;
    
    @Positive(message = "El factor de conversión debe ser mayor a 0")
    private Double factorConversion;
    
    @Positive(message = "El precio unitario debe ser mayor a 0")
    private Double precioUnitario;
    
    private Double cantidadRecibida;
    
    private Double subtotal;
    
    private String observaciones;
}