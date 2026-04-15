package com.mobilesco.mobilesco_back.models;

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
    @JoinColumn(name = "tipo_producto_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_producto_tipo"))
    private TipoProductoModel tipoProducto;

    @ManyToOne
    @JoinColumn(name = "linea_id",
                foreignKey = @ForeignKey(name = "fk_producto_linea"))
    private LineaProductoModel linea;

    @ManyToOne
    @JoinColumn(name = "categoria_id",
                foreignKey = @ForeignKey(name = "fk_producto_categoria"))
    private CategoriaModel categoria;

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

    // Un producto puede tener múltiples insumos (BOM)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductoInsumoModel> insumos = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        fechaRegistro = now;
        fechaActualizacion = now;
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}