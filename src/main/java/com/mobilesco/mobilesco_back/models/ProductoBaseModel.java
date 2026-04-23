// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/models/ProductoBaseModel.java
// ============================================
package com.mobilesco.mobilesco_back.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "productos_base")
public class ProductoBaseModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 30)
    private String sku;

    @Column(nullable = false, length = 200)
    private String nombre;
    
    @Column(length = 500)
    private String descripcion;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relaciones
    @ManyToOne
    @JoinColumn(name = "familia_id")
    private FamiliaModel familia;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
