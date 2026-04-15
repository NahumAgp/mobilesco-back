package com.mobilesco.mobilesco_back.dto.DistribucionCosto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistribucionCostoResponseDTO {
    private Long id;
    
    // Costo indirecto
    private Long costoIndirectoId;
    private String costoIndirectoCodigo;
    private String costoIndirectoNombre;
    
    // Producto
    private Long productoId;
    private String productoSku;
    private String productoNombre;
    
    private Integer anio;
    private Integer mes;
    private Double montoAsignado;
    private Double porcentajeParticipacion;
    private Double baseCalculo;
    private LocalDateTime fechaRegistro;
}