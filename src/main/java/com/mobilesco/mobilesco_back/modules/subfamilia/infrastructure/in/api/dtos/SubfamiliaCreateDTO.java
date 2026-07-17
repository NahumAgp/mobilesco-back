package com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SubfamiliaCreateDTO {

    @Size(max = 10, message = "El codigo no puede exceder 10 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La familia es obligatoria")
    @JsonProperty("familia_id")
    private Long familiaId;

    private Boolean activo;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Long getFamiliaId() { return familiaId; }
    public void setFamiliaId(Long familiaId) { this.familiaId = familiaId; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
