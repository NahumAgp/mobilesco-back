/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/categoria/infrastructure/in/api/dtos/CategoriaResponseDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: CategoriaResponseDTO
 * CONTEXTO: DTO de salida para respuestas de Categoria.
 * NOTAS: Se usa en listados y detalle de categoria.
 */
package com.mobilesco.mobilesco_back.modules.categoria.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

public class CategoriaResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    public CategoriaResponseDTO() {
    }

    public CategoriaResponseDTO(
            Long id,
            String nombre,
            String descripcion,
            Boolean activo,
            LocalDateTime fechaRegistro,
            LocalDateTime fechaActualizacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.fechaActualizacion = fechaActualizacion;
    }

    public static Builder builder() {
        return new Builder();
    }

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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public static class Builder {
        private Long id;
        private String nombre;
        private String descripcion;
        private Boolean activo;
        private LocalDateTime fechaRegistro;
        private LocalDateTime fechaActualizacion;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Builder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Builder fechaRegistro(LocalDateTime fechaRegistro) {
            this.fechaRegistro = fechaRegistro;
            return this;
        }

        public Builder fechaActualizacion(LocalDateTime fechaActualizacion) {
            this.fechaActualizacion = fechaActualizacion;
            return this;
        }

        public CategoriaResponseDTO build() {
            return new CategoriaResponseDTO(id, nombre, descripcion, activo, fechaRegistro, fechaActualizacion);
        }
    }
}
