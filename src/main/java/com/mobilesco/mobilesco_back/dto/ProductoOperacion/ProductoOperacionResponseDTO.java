package com.mobilesco.mobilesco_back.dto.ProductoOperacion;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoOperacionResponseDTO {
    private Long id;
    private Long productoId;
    private String productoSku;
    private String productoNombre;
    
    private Long operacionId;
    private String operacionCodigo;
    private String operacionNombre;
    private Double tiempoOperacion;
    private Double costoMinutoOperacion;
    private String centroTrabajoNombre;
    
    private Integer cantidad;
    private Double tiempoTotal;
    private Double importeActividad;
    private Integer orden;
    private String observaciones;
    private Boolean activo;
    
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}