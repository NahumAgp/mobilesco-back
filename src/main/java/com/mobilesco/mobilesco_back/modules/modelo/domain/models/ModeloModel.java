/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/modelo/domain/models/ModeloModel.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ModeloModel
 * CONTEXTO: Entidad JPA del modulo Modelo (producto base).
 * NOTAS: Mantiene relacion con Familia para clasificacion.
 */
package com.mobilesco.mobilesco_back.modules.modelo.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;

@Entity
@Getter
@Setter
@Table(name = "productos_base")
public class ModeloModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo", unique = true, nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;
    
    @Column(length = 500)
    private String descripcion;

    @Column(name = "url_imagen", length = 500)
    private String urlImagen;

    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relaciones
    @ManyToOne
    @JoinColumn(name = "familia_id")
    private FamiliaModel familia;

    @ManyToMany
    @JoinTable(
            name = "modelo_material",
            joinColumns = @JoinColumn(name = "modelo_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_modelo_material", columnNames = {"modelo_id", "material_id"})
    )
    private Set<MaterialModel> materiales = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
