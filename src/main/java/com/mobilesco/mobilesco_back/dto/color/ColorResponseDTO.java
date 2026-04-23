// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/color/ColorResponseDTO.java
package com.mobilesco.mobilesco_back.dto.color;

import java.time.LocalDateTime;

public class ColorResponseDTO {
    
    private Long id;
    private String nombre;
    private String hex;
    private Boolean activo;
    private LocalDateTime createdAt;
    
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
    
    public String getHex() {
        return hex;
    }
    
    public void setHex(String hex) {
        this.hex = hex;
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
}