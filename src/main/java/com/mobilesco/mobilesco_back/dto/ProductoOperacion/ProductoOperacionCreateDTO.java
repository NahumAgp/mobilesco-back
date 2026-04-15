package com.mobilesco.mobilesco_back.dto.ProductoOperacion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoOperacionCreateDTO {
    
    @NotNull(message = "El ID de la operación es requerido")
    private Long operacionId;
    
    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer cantidad;
    
    private String observaciones;
    
    private Integer orden;
}