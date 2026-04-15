package com.mobilesco.mobilesco_back.dto.Produccion;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProduccionUpdateDTO {
    
    private String folio;
    
    private Long productoId;
    
    private LocalDate fechaProduccion;
    
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    
    private String estado;  // PLANEADA, EN_PROCESO, TERMINADA, CANCELADA
    
    private LocalDateTime fechaInicio;
    
    private LocalDateTime fechaFin;
    
    private String observaciones;
    
    private Double costoTotal;
    
    private Double costoUnitario;
    
    private Boolean activo;
}