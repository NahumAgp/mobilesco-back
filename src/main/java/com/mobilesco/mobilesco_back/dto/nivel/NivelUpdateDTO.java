// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/nivel/NivelUpdateDTO.java
package com.mobilesco.mobilesco_back.dto.nivel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class NivelUpdateDTO {
    
    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^[0-9]{2}$", message = "El código debe tener 2 dígitos")
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripcion no puede exceder 255 caracteres")
    private String descripcion;
    
    private Boolean activo;
    
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
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
