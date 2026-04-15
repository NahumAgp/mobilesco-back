package com.mobilesco.mobilesco_back.dto.Produccion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduccionTiempoDTO {
    
    @NotNull(message = "La operación es obligatoria")
    private Long operacionId;
    
    private String operacionNombre;  // Para respuestas
    
    private Long centroTrabajoId;
    private String centroTrabajoNombre;  // Para respuestas
    
    @Positive(message = "Los minutos teóricos deben ser mayor a 0")
    private Double minutosTeoricos;
    
    @NotNull(message = "Los minutos reales son obligatorios")
    @Positive(message = "Los minutos reales deben ser mayor a 0")
    private Double minutosReales;
    
    private Double costoMinuto;  // Para respuestas
    private Double costoTotal;    // Para respuestas
}