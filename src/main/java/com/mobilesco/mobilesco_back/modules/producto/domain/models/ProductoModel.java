/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/domain/models/ProductoModel.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoModel
 * CONTEXTO: Entidad JPA del modulo Producto para SKU final de catalogo.
 * NOTAS: Mantiene relaciones con modelo, nivel, color, material y BOM.
 */
package com.mobilesco.mobilesco_back.modules.producto.domain.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.lineaproducto.domain.models.LineaProductoModel;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.models.ImagenModel;
import com.mobilesco.mobilesco_back.models.NivelModel;
import com.mobilesco.mobilesco_back.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.models.ProductoOperacionModel;

@Entity
@Table(
    name = "producto",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_producto_sku", columnNames = {"sku"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;  // Código único: SARP01, SF02, etc.

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    // Relaciones con catálogos
    

    @ManyToOne
    @JoinColumn(name = "linea_id",
                foreignKey = @ForeignKey(name = "fk_producto_linea"))
    private LineaProductoModel linea;

    @ManyToOne
    @JoinColumn(name = "producto_base_id")
    private ModeloModel modelo;

    @ManyToOne
    @JoinColumn(name = "nivel_id")
    private NivelModel nivel;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private ColorModel color;

    @ManyToOne
    @JoinColumn(name = "material_id",
                foreignKey = @ForeignKey(name = "fk_producto_material"))
    private MaterialModel material;

    @Column(name = "caracteristicas", length = 500)
    private String caracteristicas;  // "ASIENTO Y RESPALDO", "CON CODERAS", etc.

    @Column(name = "dimensiones", length = 100)
    private String dimensiones;  // "1.20 X .40 X .65 MTS."

    @Column(name = "peso_kg")
    private Double pesoKg;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Un producto puede tener múltiples insumos (BOM)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductoInsumoModel> insumos = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductoOperacionModel> operaciones = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImagenModel> imagenes = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        fechaRegistro = now;
        fechaActualizacion = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void preUpdate() {
        LocalDateTime now = LocalDateTime.now();
        fechaActualizacion = now;
        updatedAt = now;
    }
}
