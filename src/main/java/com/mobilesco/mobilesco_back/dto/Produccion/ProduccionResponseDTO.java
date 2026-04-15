package com.mobilesco.mobilesco_back.dto.Produccion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduccionResponseDTO {
    private Long id;
    private String folio;
    
    private Long productoId;
    private String productoSku;
    private String productoNombre;
    
    private LocalDate fechaProduccion;
    private Integer cantidad;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado;
    private String observaciones;
    
    private Double costoTotal;
    private Double costoUnitario;
    
    private List<ProduccionInsumoDTO> insumos;
    private List<ProduccionTiempoDTO> tiempos;
}