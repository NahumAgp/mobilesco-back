package com.mobilesco.mobilesco_back.dto.Producto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoInsumoResponseDTO {
    private Long id;
    private Long productoId;
    
    private Long insumoId;
    private String insumoNombre;
    private String insumoUnidad;
    private Double cantidad;
    private Double desperdicioPorcentaje;
    private Double cantidadConDesperdicio;  // cantidad * (1 + desperdicio/100)
    private String observaciones;
    private Double costoUnitario;      // Del Kardex
    private Double subtotal;           
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}