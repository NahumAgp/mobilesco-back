// RUTA: src/main/java/com/mcf/mobiliario/dto/familia/FamiliaCreateDTO.java
package com.mobilesco.mobilesco_back.dto.familia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FamiliaCreateDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
    
    @NotNull(message = "La línea es obligatoria")
    private Long lineaId;
    
    // Getters y Setters
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