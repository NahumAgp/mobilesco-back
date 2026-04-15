package com.mobilesco.mobilesco_back.dto.Producto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ProductoInsumoListaDTO {
    
    @NotEmpty(message = "Debe incluir al menos un insumo")
    private List<ProductoInsumoCreateDTO> insumos;
}