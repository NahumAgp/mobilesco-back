/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/dtos/ProductoResponseDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoResponseDTO
 * CONTEXTO: DTO de salida para respuestas del modulo Producto.
 * NOTAS: Consolidado para listados, detalle y filtros.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.dtos.ImagenResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;

    @JsonProperty("id_modelo")
    private Long modeloId;

    @JsonProperty("nombre_modelo")
    private String modeloNombre;

    private String modeloUrlImagen;
    private Long familiaId;
    private String familiaNombre;
    private Long lineaId;
    private String lineaNombre;

    @JsonProperty("id_nivel")
    private Long nivelId;

    @JsonProperty("nombre_nivel")
    private String nivelNombre;

    @JsonProperty("id_color")
    private Long colorId;

    @JsonProperty("nombre_color")
    private String colorNombre;

    @JsonProperty("id_material")
    private Long materialId;

    @JsonProperty("nombre_material")
    private String materialNombre;

    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ImagenResponseDTO imagenPrincipal;
    private List<ImagenResponseDTO> imagenes;

    private List<ProductoInsumoResponseDTO> insumos;
    private List<ProductoOperacionResponseDTO> operaciones;

    public void setId(Long id) {
        this.id = id;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setModeloId(Long modeloId) {
        this.modeloId = modeloId;
    }

    public void setModeloNombre(String modeloNombre) {
        this.modeloNombre = modeloNombre;
    }

    public void setModeloUrlImagen(String modeloUrlImagen) {
        this.modeloUrlImagen = modeloUrlImagen;
    }

    public void setFamiliaId(Long familiaId) {
        this.familiaId = familiaId;
    }

    public void setFamiliaNombre(String familiaNombre) {
        this.familiaNombre = familiaNombre;
    }

    public void setLineaId(Long lineaId) {
        this.lineaId = lineaId;
    }

    public void setLineaNombre(String lineaNombre) {
        this.lineaNombre = lineaNombre;
    }

    public void setNivelId(Long nivelId) {
        this.nivelId = nivelId;
    }

    public void setNivelNombre(String nivelNombre) {
        this.nivelNombre = nivelNombre;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
    }

    public void setColorNombre(String colorNombre) {
        this.colorNombre = colorNombre;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public void setMaterialNombre(String materialNombre) {
        this.materialNombre = materialNombre;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setImagenPrincipal(ImagenResponseDTO imagenPrincipal) {
        this.imagenPrincipal = imagenPrincipal;
    }

    public void setImagenes(List<ImagenResponseDTO> imagenes) {
        this.imagenes = imagenes;
    }

    public void setInsumos(List<ProductoInsumoResponseDTO> insumos) {
        this.insumos = insumos;
    }

    public void setOperaciones(List<ProductoOperacionResponseDTO> operaciones) {
        this.operaciones = operaciones;
    }
}
