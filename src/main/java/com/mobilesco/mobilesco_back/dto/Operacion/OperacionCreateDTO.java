package com.mobilesco.mobilesco_back.dto.Operacion;

import jakarta.validation.constraints.NotBlank;
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
public class OperacionCreateDTO {
    
    @NotBlank(message = "El código es requerido")
    private String codigo;
    
    @NotBlank(message = "El nombre es requerido")
    private String nombre;
    
    private String descripcion;
    
    @NotNull(message = "El tiempo de operación es requerido")
    @Positive(message = "El tiempo debe ser mayor a 0")
    private Double tiempoOperacion;  // 🔴 ESTE CAMPO ES EL QUE FALTA
    
    private Double costoHora; 
    private Double costoMinuto;
    
    @NotNull(message = "El centro de trabajo es requerido")
    private Long centroTrabajoId;
}