package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ModeloCategoriaDTO {

    private Long id;

    @Pattern(regexp = "^[A-Z0-9]{1,3}$", message = "El codigo debe tener de 1 a 3 letras o numeros")
    private String codigo;

    @NotBlank(message = "El nombre de la categoria es obligatorio")
    @Size(max = 50, message = "El nombre de la categoria no puede exceder 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripcion de la categoria no puede exceder 255 caracteres")
    private String descripcion;

    private Boolean activo;

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
}
