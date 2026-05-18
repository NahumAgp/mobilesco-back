/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/infrastructure/in/api/dtos/ColorResponseDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ColorResponseDTO
 * CONTEXTO: DTO de salida para respuestas del modulo Color.
 * NOTAS: Expuesto en endpoints de consulta y detalle.
 */
package com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

public class ColorResponseDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
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
