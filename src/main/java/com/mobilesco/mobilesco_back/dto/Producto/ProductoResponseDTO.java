package com.mobilesco.mobilesco_back.dto.Producto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionResponseDTO;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenResponseDTO;

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

    @JsonProperty("id_modelo")
    private Long modeloId;

    @JsonProperty("nombre_modelo")
    private String modeloNombre;

    @JsonProperty("id_nivel")
    private Long nivelId;

    @JsonProperty("nombre_nivel")
    private String nivelNombre;

    @JsonProperty("id_color")
    private Long colorId;

    @JsonProperty("nombre_color")
    private String colorNombre;

    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ImagenResponseDTO imagenPrincipal;
    private List<ImagenResponseDTO> imagenes;

    private List<ProductoInsumoResponseDTO> insumos;
    private List<ProductoOperacionResponseDTO> operaciones;
}
