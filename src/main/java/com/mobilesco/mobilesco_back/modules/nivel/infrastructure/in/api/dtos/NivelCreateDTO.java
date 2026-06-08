package com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NivelCreateDTO {

    // Se conserva por compatibilidad; el servidor genera el codigo.
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripcion no puede exceder 255 caracteres")
    private String descripcion;

    @NotNull(message = "El modelo es obligatorio")
    @JsonProperty("modelo_id")
    @JsonAlias({"id_modelo", "producto_base_id", "modeloId"})
    private Long modeloId;

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

    public Long getModeloId() {
        return modeloId;
    }

    public void setModeloId(Long modeloId) {
        this.modeloId = modeloId;
    }
}
