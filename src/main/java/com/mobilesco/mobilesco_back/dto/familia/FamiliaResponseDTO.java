// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/familia/FamiliaResponseDTO.java

package com.mobilesco.mobilesco_back.dto.familia;

import java.time.LocalDateTime;

public class FamiliaResponseDTO {
    
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private String lineaNombre;
    private Long lineaId;
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
    
    public String getLineaNombre() {
        return lineaNombre;
    }
    
    public void setLineaNombre(String lineaNombre) {
        this.lineaNombre = lineaNombre;
    }
    
    public Long getLineaId() {
        return lineaId;
    }
    
    public void setLineaId(Long lineaId) {
        this.lineaId = lineaId;
    }
}
