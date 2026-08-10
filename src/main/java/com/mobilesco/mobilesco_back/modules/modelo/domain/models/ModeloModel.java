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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import com.mobilesco.mobilesco_back.modules.subfamilia.domain.models.SubfamiliaModel;

@Entity
@Getter
@Setter
@Table(
    name = "productos_base",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_modelo_clasificacion_codigo", columnNames = {"familia_id", "subfamilia_id", "codigo"}),
        @UniqueConstraint(name = "uk_modelo_clasificacion_nombre", columnNames = {"familia_id", "subfamilia_id", "nombre"})
    }
)
public class ModeloModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;
    
    @Column(length = 500)
    private String descripcion;

    @Column(name = "descripcion_corta", length = 250)
    private String descripcionCorta;

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

    @ManyToOne
    @JoinColumn(name = "subfamilia_id")
    private SubfamiliaModel subfamilia;

    @ManyToMany
    @JoinTable(
            name = "modelo_material",
            joinColumns = @JoinColumn(name = "modelo_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_modelo_material", columnNames = {"modelo_id", "material_id"})
    )
    private Set<MaterialModel> materiales = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "modelo_insumo",
            joinColumns = @JoinColumn(name = "modelo_id"),
            inverseJoinColumns = @JoinColumn(name = "insumo_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_modelo_insumo", columnNames = {"modelo_id", "insumo_id"})
    )
    private Set<InsumoModel> insumos = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "modelo_operacion",
            joinColumns = @JoinColumn(name = "modelo_id"),
            inverseJoinColumns = @JoinColumn(name = "operacion_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_modelo_operacion", columnNames = {"modelo_id", "operacion_id"})
    )
    @OrderColumn(name = "orden")
    private List<OperacionModel> operaciones = new ArrayList<>();
    
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
