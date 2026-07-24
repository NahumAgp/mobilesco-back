package com.mobilesco.mobilesco_back.modules.operacion.infrastructure.in.api.dtos;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OperacionUpdateDTO {
    
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    @NotNull(message = "El tiempo de operación es requerido")
    @Positive(message = "El tiempo debe ser mayor a 0")
    @Column(name = "tiempo_operacion", nullable = false)
    private Double tiempoOperacion;  // Tiempo estándar en minutos por unidad
    
    private Long centroTrabajoId;
    
    @NotNull(message = "El costo por minuto es requerido")
    @Positive(message = "El costo por minuto debe ser mayor a 0")
    private Double costoMinuto;
    
    private Boolean activo;
}
