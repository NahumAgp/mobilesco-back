package com.mobilesco.mobilesco_back.modules.compra.domain.models;

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

    @Column(nullable = false)
    private Double precioUnitario;

    private Double cantidadRecibida;

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
            subtotal = cantidad * precioUnitario;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
