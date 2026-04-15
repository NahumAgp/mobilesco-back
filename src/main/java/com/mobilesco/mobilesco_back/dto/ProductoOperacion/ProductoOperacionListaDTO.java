package com.mobilesco.mobilesco_back.dto.ProductoOperacion;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ProductoOperacionListaDTO {
    
    @NotEmpty(message = "Debe incluir al menos una operación")
    private List<ProductoOperacionCreateDTO> operaciones;
}