// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/estilo/EstiloResponseDTO.java
package com.mobilesco.mobilesco_back.dto.estilo;

import java.time.LocalDateTime;

public class EstiloResponseDTO {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private String familiaNombre;
    private Long familiaId;
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getFamiliaNombre() {
        return familiaNombre;
    }
    
    public void setFamiliaNombre(String familiaNombre) {
        this.familiaNombre = familiaNombre;
    }
    
    public Long getFamiliaId() {
        return familiaId;
    }
    
    public void setFamiliaId(Long familiaId) {
        this.familiaId = familiaId;
    }
}