// ============================================
// RUTA: src/main/java/com/mcf/mobiliario/dto/imagen/ImagenResponseDTO.java
// ============================================
package com.mobilesco.mobilesco_back.dto.imagen;

import java.time.LocalDateTime;

public class ImagenResponseDTO {
    
    private Long id;
    private String url;
    private Boolean esPrincipal;
    private Integer orden;
    private String altTexto;
    private LocalDateTime createdAt;
    private Long productoId;
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }
    
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    
    public String getAltTexto() { return altTexto; }
    public void setAltTexto(String altTexto) { this.altTexto = altTexto; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
}
