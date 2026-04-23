// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/color/ColorUpdateDTO.java

package com.mobilesco.mobilesco_back.dto.color;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ColorUpdateDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30, message = "El nombre no puede exceder 30 caracteres")
    private String nombre;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El código hexadecimal debe tener formato #RRGGBB")
    private String hex;
    
    private Boolean activo;
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getHex() {
        return hex;
    }
    
    public void setHex(String hex) {
        this.hex = hex;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}