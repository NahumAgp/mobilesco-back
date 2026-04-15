package com.mobilesco.mobilesco_back.dto.Operacion;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperacionResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    
    // Datos del centro de trabajo (si aplica)
    private Long centroTrabajoId;
    private String centroTrabajoNombre;
    
    private Double costoMinuto;
    private Double costoHora;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private Double tiempoOperacion;  
}