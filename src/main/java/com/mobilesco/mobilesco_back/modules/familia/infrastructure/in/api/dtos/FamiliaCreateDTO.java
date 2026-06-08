/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/infrastructure/in/api/dtos/FamiliaCreateDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: FamiliaCreateDTO
 * CONTEXTO: DTO de entrada para crear una familia.
 * NOTAS: Requiere lineaId para asociar la familia a una linea existente.
 */
package com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FamiliaCreateDTO {

    // Se conserva por compatibilidad; el servidor genera el codigo.
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
    
    @NotNull(message = "La línea es obligatoria")
    private Long lineaId;
    
    // Getters y Setters
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
    
    public Long getLineaId() {
        return lineaId;
    }
    
    public void setLineaId(Long lineaId) {
        this.lineaId = lineaId;
    }
}
