package com.mobilesco.mobilesco_back.dto.Kardex;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInsumoResponseDTO {
    private Long id;
    
    // Datos del insumo
    private Long insumoId;
    private String insumoNombre;
    private String insumoUnidad;
    
    private LocalDateTime fecha;
    private String tipo;
    private String concepto;
    private Double cantidad;
    private Double costoUnitario;
    private Double costoTotal;
    private String documento;
    private String referencia;
    private String observaciones;
    private Double stockAnterior;
    private Double stockNuevo;
    private String usuario;
    
    // Referencias
    private Long compraId;
    private Long produccionId;
    private Long ajusteId;
    
    private LocalDateTime fechaRegistro;
}