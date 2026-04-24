// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/variante/VarianteResponseDTO.java
// ============================================
package com.mobilesco.mobilesco_back.dto.variante;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class VarianteResponseDTO {

    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonProperty("id_producto_base")
    private Long productoBaseId;
    @JsonProperty("id_nivel")
    private Long nivelId;
    @JsonProperty("id_color")
    private Long colorId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getProductoBaseId() { return productoBaseId; }
    public void setProductoBaseId(Long productoBaseId) { this.productoBaseId = productoBaseId; }

    public Long getNivelId() { return nivelId; }
    public void setNivelId(Long nivelId) { this.nivelId = nivelId; }

    public Long getColorId() { return colorId; }
    public void setColorId(Long colorId) { this.colorId = colorId; }
}
