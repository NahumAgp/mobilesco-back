package com.mobilesco.mobilesco_back.dto.Producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoUpdateDTO {
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    private String sku;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;
    
    @Size(max = 500)
    private String descripcion;
    
    private Long tipoProductoId;
    private Long lineaId;
    private Long categoriaId;
    private Long materialId;
    
    @Size(max = 500)
    private String caracteristicas;
    
    @Size(max = 100)
    private String dimensiones;
    
    private Double pesoKg;
    private Boolean activo;
}