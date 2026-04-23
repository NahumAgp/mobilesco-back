// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/estilo/EstiloCreateDTO.java
package com.mobilesco.mobilesco_back.dto.estilo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EstiloCreateDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
    
    @NotNull(message = "La familia es obligatoria")
    private Long familiaId;
    
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
    
    public Long getFamiliaId() {
        return familiaId;
    }
    
    public void setFamiliaId(Long familiaId) {
        this.familiaId = familiaId;
    }
}