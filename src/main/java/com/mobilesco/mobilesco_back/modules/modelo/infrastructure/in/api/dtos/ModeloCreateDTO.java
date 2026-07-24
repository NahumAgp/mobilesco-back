// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/modelo/ModeloCreateDTO.java
// ============================================
/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/modelo/infrastructure/in/api/dtos/ModeloCreateDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ModeloCreateDTO
 * CONTEXTO: DTO de entrada para crear un modelo base.
 * NOTAS: Incluye datos clave y referencia a familia.
 */
package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ModeloCreateDTO {

    // Se conserva por compatibilidad; el servidor genera el codigo.
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    private String descripcion;

    @Size(max = 250, message = "La descripcion corta no puede exceder 250 caracteres")
    private String descripcionCorta;

    @Size(max = 500, message = "La url de imagen no puede exceder 500 caracteres")
    @JsonProperty("url_imagen")
    private String urlImagen;

    @NotNull(message = "La familia es obligatoria")
    @JsonProperty("familia_id")
    private Long familiaId;

    @JsonProperty("subfamilia_id")
    private Long subfamiliaId;

    private Boolean activo;

    @Valid
    @NotNull(message = "Las categorias del modelo son obligatorias")
    @Size(min = 1, message = "El modelo debe tener al menos una categoria")
    private List<ModeloCategoriaDTO> categorias;

    @JsonProperty("materiales")
    private List<Long> materiales;

    @JsonProperty("insumos")
    private List<Long> insumos;

    @JsonProperty("operaciones")
    private List<Long> operaciones;

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

    public String getDescripcionCorta() {
        return descripcionCorta;
    }

    public void setDescripcionCorta(String descripcionCorta) {
        this.descripcionCorta = descripcionCorta;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public Long getFamiliaId() {
        return familiaId;
    }

    public void setFamiliaId(Long familiaId) {
        this.familiaId = familiaId;
    }

    public Long getSubfamiliaId() {
        return subfamiliaId;
    }

    public void setSubfamiliaId(Long subfamiliaId) {
        this.subfamiliaId = subfamiliaId;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<ModeloCategoriaDTO> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<ModeloCategoriaDTO> categorias) {
        this.categorias = categorias;
    }

    public List<Long> getMateriales() {
        return materiales;
    }

    public void setMateriales(List<Long> materiales) {
        this.materiales = materiales;
    }

    public List<Long> getInsumos() {
        return insumos;
    }

    public void setInsumos(List<Long> insumos) {
        this.insumos = insumos;
    }

    public List<Long> getOperaciones() {
        return operaciones;
    }

    public void setOperaciones(List<Long> operaciones) {
        this.operaciones = operaciones;
    }
}
