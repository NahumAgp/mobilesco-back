package com.mobilesco.mobilesco_back.dto.CentroTrabajo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CentroTrabajoCreateDTO {
    
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
    
    @NotNull(message = "El costo por hora es obligatorio")
    @Positive(message = "El costo por hora debe ser mayor a 0")
    private Double costoHora;
    
    @Positive(message = "La capacidad diaria debe ser mayor a 0")
    private Double capacidadDiaria;
    
    @Size(max = 50)
    private String unidadCapacidad;
    
    @Positive(message = "Las horas disponibles deben ser mayor a 0")
    private Double horasDisponiblesDia;
}