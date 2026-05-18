// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/linea/LineaResponseDTO.java
/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/linea/infrastructure/in/api/dtos/LineaResponseDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaResponseDTO
 * CONTEXTO: DTO de salida para respuestas de Linea.
 * NOTAS: Incluye metadatos de creacion para UI.
 */
package com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

public class LineaResponseDTO {
    
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Integer orden;
    private Boolean activo;
    private LocalDateTime createdAt;
    
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
    
    public Integer getOrden() {
        return orden;
    }
    
    public void setOrden(Integer orden) {
        this.orden = orden;
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
