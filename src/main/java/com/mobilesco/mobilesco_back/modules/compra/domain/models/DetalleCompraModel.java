package com.mobilesco.mobilesco_back.modules.compra.domain.models;

import com.mobilesco.mobilesco_back.modules.shared.infrastructure.persistence.CostoDecimalConverter;
import com.mobilesco.mobilesco_back.modules.shared.application.costos.CostoDecimal;
import jakarta.persistence.Convert;
import java.math.RoundingMode;

import java.time.LocalDateTime;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

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

    @ManyToOne
    @JoinColumn(name = "unidad_compra_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_unidad_compra"))
    private UnidadMedidaModel unidadCompra;

    @Column(nullable = false)
    private Double cantidad;

    @Column(nullable = false)
    private Double factorConversion;

    @Convert(converter = CostoDecimalConverter.class)
    @Column(nullable = false, precision = 18, scale = 6)
    private Double precioUnitario;

    private Double cantidadRecibida;

    @Convert(converter = CostoDecimalConverter.class)
    @Column(precision = 18, scale = 6)
    private Double subtotal;

    @Column(length = 255)
    private String observaciones;

    @Column(name = "motivo_no_recepcion", length = 255)
    private String motivoNoRecepcion;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Transient
    public Double getCantidadEnUnidadConsumo() {
        return (cantidadRecibida != null ? cantidadRecibida : 0.0) * factorConversion;
    }

    @Transient
    public Double getCostoPorUnidadConsumo() {
        if (factorConversion == null || factorConversion <= 0 || precioUnitario == null) return 0.0;
        return CostoDecimal.de(precioUnitario).divide(CostoDecimal.de(factorConversion), 6,
                RoundingMode.HALF_UP).doubleValue();
    }

    @Transient
    public Double getTotalLinea() {
        if (subtotal != null) {
            return subtotal;
        }
        if (cantidad != null && precioUnitario != null) {
            return CostoDecimal.de(cantidad).multiply(CostoDecimal.de(precioUnitario)).doubleValue();
        }
        return 0.0;
    }

    @Transient
    public Double getCantidadPendiente() {
        double recibida = cantidadRecibida != null ? cantidadRecibida : 0.0;
        double comprada = cantidad != null ? cantidad : 0.0;
        return Math.max(comprada - recibida, 0.0);
    }

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        fechaRegistro = now;
        fechaActualizacion = now;

        if (cantidadRecibida == null) {
            cantidadRecibida = 0.0;
        }

        if (subtotal == null) {
            subtotal = CostoDecimal.de(cantidad).multiply(CostoDecimal.de(precioUnitario)).doubleValue();
        }
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
