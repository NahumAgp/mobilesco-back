package com.mobilesco.mobilesco_back.modules.producto.domain.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.persistence.CostoDecimalConverter;

@Entity
@Table(
    name = "producto_insumo",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_producto_insumo", columnNames = {"producto_id", "insumo_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoInsumoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_bom_producto"))
    private ProductoModel producto;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_bom_insumo"))
    private InsumoModel insumo;

    @Column(name = "cantidad")
    private Double cantidad;  // En unidad de medida del insumo

    @Column(name = "desperdicio_porcentaje")
    @Builder.Default
    private Double desperdicioPorcentaje = 0.0;

    @Convert(converter = CostoDecimalConverter.class)
    @Column(name = "costo_unitario_inicial", precision = 18, scale = 6)
    private Double costoUnitarioInicial;

    @Column(name = "costo_inicial", nullable = false)
    @Builder.Default
    private Boolean costoInicial = false;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public double getCostoUnitarioVigente() {
        if (costoUnitarioInicial != null) return costoUnitarioInicial;
        return insumo != null && insumo.getCostoCotizacion() != null ? insumo.getCostoCotizacion() : 0.0;
    }

    public LocalDateTime getFechaCostoCotizacionVigente() {
        if (costoUnitarioInicial != null) return fechaRegistro;
        return insumo != null ? insumo.getFechaCostoCotizacion() : null;
    }

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
