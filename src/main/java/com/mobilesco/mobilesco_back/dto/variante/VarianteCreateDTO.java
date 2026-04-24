// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/variante/VarianteCreateDTO.java
// ============================================
package com.mobilesco.mobilesco_back.dto.variante;

import com.fasterxml.jackson.annotation.JsonProperty;
 
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VarianteCreateDTO {

    @Size(max = 30, message = "El sku no puede exceder 30 caracteres")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El id_producto_base es obligatorio")
    @JsonProperty("id_producto_base")
    private Long productoBaseId;

    @NotNull(message = "El id_nivel es obligatorio")
    @JsonProperty("id_nivel")
    private Long nivelId;

    @NotNull(message = "El id_color es obligatorio")
    @JsonProperty("id_color")
    private Long colorId;

    private Boolean activo;

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

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
