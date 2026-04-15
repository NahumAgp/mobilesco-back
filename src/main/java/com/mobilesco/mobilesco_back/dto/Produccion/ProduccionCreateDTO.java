package com.mobilesco.mobilesco_back.dto.Produccion;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProduccionCreateDTO {
    
    @NotBlank(message = "El folio es obligatorio")
    private String folio;
    
    @NotNull(message = "El producto es obligatorio")
    private Long productoId;
    
    private LocalDate fechaProduccion;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    
    private String observaciones;
}