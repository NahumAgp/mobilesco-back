// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/imagen/ImagenUpdateDTO.java
// ============================================
package com.mobilesco.mobilesco_back.dto.imagen;

import jakarta.validation.constraints.Size;

public class ImagenUpdateDTO {
    
    @Size(max = 500, message = "La URL no puede exceder 500 caracteres")
    private String url;
    
    private Boolean esPrincipal;
    
    private Integer orden;
    
    @Size(max = 200, message = "El texto alternativo no puede exceder 200 caracteres")
    private String altTexto;
    
    // Getters y Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }
    
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    
    public String getAltTexto() { return altTexto; }
    public void setAltTexto(String altTexto) { this.altTexto = altTexto; }
}