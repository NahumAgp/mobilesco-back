package com.mobilesco.mobilesco_back.modules.compra.domain.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
        name = "cuenta_por_pagar",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cuenta_por_pagar_compra", columnNames = {"compra_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaPorPagarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cuenta_por_pagar_compra"))
    private CompraModel compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cuenta_por_pagar_proveedor"))
    private ProveedorModel proveedor;

    @Column(name = "fecha_cuenta", nullable = false)
    private LocalDate fechaCuenta;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "monto_total", nullable = false)
    private Double montoTotal;

    @Column(name = "monto_pagado", nullable = false)
    private Double montoPagado;

    @Column(name = "saldo_pendiente", nullable = false)
    private Double saldoPendiente;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "cuentaPorPagar", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PagoCuentaPorPagarModel> pagos = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        fechaRegistro = now;
        fechaActualizacion = now;
        if (fechaCuenta == null) {
            fechaCuenta = LocalDate.now();
        }
        if (montoPagado == null) {
            montoPagado = 0.0;
        }
        if (saldoPendiente == null) {
            saldoPendiente = montoTotal != null ? montoTotal : 0.0;
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
