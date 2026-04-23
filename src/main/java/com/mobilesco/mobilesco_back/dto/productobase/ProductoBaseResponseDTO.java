// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/productobase/ProductoBaseResponseDTO.java
// ============================================
package com.mobilesco.mobilesco_back.dto.productobase;

import java.time.LocalDateTime;

public class ProductoBaseResponseDTO {

    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long familiaId;
    private String familiaNombre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getFamiliaId() {
        return familiaId;
    }

    public void setFamiliaId(Long familiaId) {
        this.familiaId = familiaId;
    }

    public String getFamiliaNombre() {
        return familiaNombre;
    }

    public void setFamiliaNombre(String familiaNombre) {
        this.familiaNombre = familiaNombre;
    }
}
