
// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/familia/FamiliaUpdateDTO.java
package com.mobilesco.mobilesco_back.dto.familia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FamiliaUpdateDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
    
    private Long lineaId;
    
    private Boolean activo;
    
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
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
