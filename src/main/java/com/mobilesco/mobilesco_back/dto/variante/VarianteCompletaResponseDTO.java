// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/variante/VarianteCompletaResponseDTO.java
// ============================================
package com.mobilesco.mobilesco_back.dto.variante;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenResponseDTO;

public class VarianteCompletaResponseDTO {

    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;

    @JsonProperty("id_producto_base")
    private Long productoBaseId;
    @JsonProperty("id_nivel")
    private Long nivelId;
    @JsonProperty("id_color")
    private Long colorId;

    private ImagenResponseDTO imagenPrincipal;
    private List<ImagenResponseDTO> imagenes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getProductoBaseId() { return productoBaseId; }
    public void setProductoBaseId(Long productoBaseId) { this.productoBaseId = productoBaseId; }

    public Long getNivelId() { return nivelId; }
    public void setNivelId(Long nivelId) { this.nivelId = nivelId; }

    public Long getColorId() { return colorId; }
    public void setColorId(Long colorId) { this.colorId = colorId; }

    public ImagenResponseDTO getImagenPrincipal() { return imagenPrincipal; }
    public void setImagenPrincipal(ImagenResponseDTO imagenPrincipal) { this.imagenPrincipal = imagenPrincipal; }

    public List<ImagenResponseDTO> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenResponseDTO> imagenes) { this.imagenes = imagenes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
