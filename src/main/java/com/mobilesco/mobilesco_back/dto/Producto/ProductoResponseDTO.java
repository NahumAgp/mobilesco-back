package com.mobilesco.mobilesco_back.dto.Producto;

import java.time.LocalDateTime;
import java.util.List;

import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;
    
    private Long tipoProductoId;
    private String tipoProductoNombre;
    
    private Long lineaId;
    private String lineaNombre;
    
    private Long categoriaId;
    private String categoriaNombre;
    
    private Long materialId;
    private String materialNombre;
    
    private String caracteristicas;
    private String dimensiones;
    private Double pesoKg;
    private Boolean activo;
    
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    
    private List<ProductoInsumoResponseDTO> insumos;
    private List<ProductoOperacionResponseDTO> operaciones; 
}