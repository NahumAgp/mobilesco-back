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
public class ProduccionInsumoDTO {
    
    @NotNull(message = "El insumo es obligatorio")
    private Long insumoId;
    
    private String insumoNombre;  // Para respuestas
    
    @Positive(message = "La cantidad teórica debe ser mayor a 0")
    private Double cantidadTeorica;
    
    @NotNull(message = "La cantidad real es obligatoria")
    @Positive(message = "La cantidad real debe ser mayor a 0")
    private Double cantidadReal;
    
    private Double costoUnitario;  // Para respuestas
    private Double costoTotal;      // Para respuestas
}