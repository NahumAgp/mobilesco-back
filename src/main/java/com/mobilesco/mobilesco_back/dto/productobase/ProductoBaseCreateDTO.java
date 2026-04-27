// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/productobase/ProductoBaseCreateDTO.java
// ============================================
package com.mobilesco.mobilesco_back.dto.productobase;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductoBaseCreateDTO {

    @NotBlank(message = "El codigo es obligatorio")
    @Size(max = 30, message = "El codigo no puede exceder 30 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    private String descripcion;

    @Size(max = 500, message = "La url de imagen no puede exceder 500 caracteres")
    @JsonProperty("url_imagen")
    private String urlImagen;

    @NotNull(message = "La familia es obligatoria")
    @JsonProperty("familia_id")
    private Long familiaId;

    private Boolean activo;

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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
