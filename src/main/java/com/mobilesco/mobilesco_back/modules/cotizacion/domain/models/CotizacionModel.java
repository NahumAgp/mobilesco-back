package com.mobilesco.mobilesco_back.modules.cotizacion.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cotizacion", uniqueConstraints = @UniqueConstraint(name = "uk_cotizacion_folio", columnNames = "folio"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizacionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String folio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cotizacion_cliente"))
    private ClienteModel cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCotizacion estado;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "margen_porcentaje", nullable = false, precision = 7, scale = 2)
    private BigDecimal margenPorcentaje;

    @Column(name = "descuento_porcentaje", nullable = false, precision = 7, scale = 2)
    private BigDecimal descuentoPorcentaje;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal flete;

    @Column(name = "iva_porcentaje", nullable = false, precision = 7, scale = 2)
    private BigDecimal ivaPorcentaje;

    @Column(name = "subtotal_costos", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotalCostos;

    @Column(name = "subtotal_venta", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotalVenta;

    @Column(name = "monto_descuento", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoDescuento;

    @Column(name = "subtotal_con_flete", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotalConFlete;

    @Column(name = "monto_iva", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoIva;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(length = 1000)
    private String notas;

    @Column(length = 1000)
    private String condiciones;

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CotizacionDetalleModel> detalles = new ArrayList<>();

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        fechaRegistro = now;
        fechaActualizacion = now;
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public void agregarDetalle(CotizacionDetalleModel detalle) {
        detalle.setCotizacion(this);
        detalles.add(detalle);
    }
}
