/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/dtos/ProductoUpdateDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoUpdateDTO
 * CONTEXTO: DTO de entrada para actualizar productos.
 * NOTAS: Permite mantener SKU y atributos de catalogo.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoUpdateDTO {

    @NotBlank(message = "El sku es obligatorio")
    @Size(max = 30, message = "El sku no puede exceder 30 caracteres")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    private String descripcion;

    @Size(max = 250, message = "La descripcion corta no puede exceder 250 caracteres")
    private String descripcionCorta;

    private Double pesoVolumetrico;

    private Double ancho;

    private Double alto;

    private Double fondo;
    @Size(max = 100, message = "Las dimensiones no pueden exceder 100 caracteres")
    private String dimensiones;

    @DecimalMin(value = "0.0", inclusive = true, message = "El peso volumetrico no puede ser negativo")
    private Double pesoKg;

    @JsonProperty("id_modelo")
    private Long modeloId;

    @JsonProperty("id_nivel")
    private Long nivelId;

    @JsonProperty("id_color")
    private Long colorId;

    @JsonProperty("id_material")
    private Long materialId;

    private Boolean activo;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public Double getPesoVolumetrico() {
        return pesoVolumetrico;
    }

    public void setPesoVolumetrico(Double pesoVolumetrico) {
        this.pesoVolumetrico = pesoVolumetrico;
    }

    public Double getAncho() {
        return ancho;
    }

    public void setAncho(Double ancho) {
        this.ancho = ancho;
    }

    public Double getAlto() {
        return alto;
    }

    public void setAlto(Double alto) {
        this.alto = alto;
    }

    public Double getFondo() {
        return fondo;
    }

    public void setFondo(Double fondo) {
        this.fondo = fondo;
    }

    public String getDimensiones() {
        return dimensiones;
    }

    public void setDimensiones(String dimensiones) {
        this.dimensiones = dimensiones;
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Long getModeloId() {
        return modeloId;
    }

    public void setModeloId(Long modeloId) {
        this.modeloId = modeloId;
    }

    public Long getNivelId() {
        return nivelId;
    }

    public void setNivelId(Long nivelId) {
        this.nivelId = nivelId;
    }

    public Long getColorId() {
        return colorId;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
