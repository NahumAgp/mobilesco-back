// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/productobase/ProductoBaseUpdateDTO.java
// ============================================
package com.mobilesco.mobilesco_back.dto.productobase;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public class ProductoBaseUpdateDTO {

    @Size(max = 30, message = "El sku no puede exceder 30 caracteres")
    private String sku;

    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    private String descripcion;

    @JsonProperty("familia_id")
    private Long familiaId;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getFamiliaId() {
        return familiaId;
    }

    public void setFamiliaId(Long familiaId) {
        this.familiaId = familiaId;
    }
}
