package com.mobilesco.mobilesco_back.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCompraModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_detalle_compra"))
    private CompraModel compra;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_detalle_insumo"))
    private InsumoModel insumo;

    // 📦 UNIDAD DE COMPRA (cómo se compró: TRAMO, CAJA, KG, ROLLO)
    @ManyToOne
    @JoinColumn(name = "unidad_compra_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_detalle_unidad_compra"))
    private UnidadMedidaModel unidadCompra;

    // 🔢 CANTIDAD COMPRADA (en unidad de compra)
    @Column(nullable = false)
    private Double cantidad;

    // 🔄 FACTOR DE CONVERSIÓN (1 unidad compra = X unidad consumo)
    @Column(nullable = false)
    private Double factorConversion;

    // 💰 PRECIO POR UNIDAD DE COMPRA
    @Column(nullable = false)
    private Double precioUnitario;

    // 📦 CANTIDAD RECIBIDA (opcional, por si llega incompleto)
    private Double cantidadRecibida;

    // 🧮 SUBTOTAL (cantidad * precioUnitario)
    private Double subtotal;

    @Column(length = 255)
    private String observaciones;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // 📊 CAMPOS CALCULADOS (no persisten en BD)

    @Transient
    public Double getCantidadEnUnidadConsumo() {
        return (cantidadRecibida != null ? cantidadRecibida : cantidad) * factorConversion;
    }

    @Transient
    public Double getCostoPorUnidadConsumo() {
        return precioUnitario / factorConversion;
    }

    @Transient
    public Double getTotalLinea() {
        if (subtotal != null) {
            return subtotal;
        }
        if (cantidad != null && precioUnitario != null) {
            return cantidad * precioUnitario;
        }
        return 0.0;
    }

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        fechaRegistro = now;
        fechaActualizacion = now;
        
        // Si no hay cantidad recibida, asumir que se recibió todo
        if (cantidadRecibida == null) {
            cantidadRecibida = cantidad;
        }
        
        // Calcular subtotal si no se proporcionó
        if (subtotal == null) {
            subtotal = cantidad * precioUnitario;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}