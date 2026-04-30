// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/models/ImagenModel.java
// ============================================
package com.mobilesco.mobilesco_back.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "imagenes", indexes = {
    @Index(name = "idx_producto_id", columnList = "producto_id"),
    @Index(name = "idx_es_principal", columnList = "es_principal")
})
public class ImagenModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String url;
    
    @Column(name = "es_principal")
    private Boolean esPrincipal = false;
    
    private Integer orden = 0;
    
    @Column(name = "alt_texto", length = 200)
    private String altTexto;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private ProductoModel producto;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
