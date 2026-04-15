package com.mobilesco.mobilesco_back.dto.Producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoCreateDTO {
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    private String sku;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "El tipo de producto es obligatorio")
    private Long tipoProductoId;
    
    private Long lineaId;
    private Long categoriaId;
    private Long materialId;
    
    @Size(max = 500)
    private String caracteristicas;
    
    @Size(max = 100)
    private String dimensiones;
    
    private Double pesoKg;
}