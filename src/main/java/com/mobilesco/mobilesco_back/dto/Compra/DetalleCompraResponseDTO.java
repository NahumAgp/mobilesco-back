package com.mobilesco.mobilesco_back.dto.Compra;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCompraResponseDTO {
    private Long id;
    private Long compraId;
    
    // Datos del insumo
    private Long insumoId;
    private String insumoNombre;
    private String insumoDescripcion;
    
    // Unidad de consumo (del insumo)
    private Long unidadConsumoId;
    private String unidadConsumoNombre;
    private String unidadConsumoSimbolo;
    
    // Unidad de compra (de este detalle)
    private Long unidadCompraId;
    private String unidadCompraNombre;
    private String unidadCompraSimbolo;
    
    // Cantidades
    private Double cantidad;                // en unidad de compra
    private Double factorConversion;        // 1 unidad compra = X unidad consumo
    private Double cantidadRecibida;        // en unidad de compra
    private Double cantidadEnUnidadConsumo; // calculado: (cantidadRecibida * factor)
    
    // Precios
    private Double precioUnitario;          // por unidad de compra
    private Double costoPorUnidadConsumo;   // calculado: precioUnitario / factor
    private Double subtotal;                 // cantidad * precioUnitario
    
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}