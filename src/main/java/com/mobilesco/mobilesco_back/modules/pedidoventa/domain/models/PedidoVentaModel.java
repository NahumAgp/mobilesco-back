package com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.CotizacionModel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pedido_venta")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoVentaModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String folio;
    @OneToOne @JoinColumn(name = "cotizacion_id", unique = true)
    private CotizacionModel cotizacion;
    @ManyToOne @JoinColumn(name = "cliente_id")
    private ClienteModel cliente;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private EstadoPedidoVenta estado;
    @Column(name = "fecha_pedido", nullable = false)
    private LocalDate fechaPedido;
    @Column(name = "fecha_compromiso")
    private LocalDate fechaCompromiso;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;
    @Column(length = 1000)
    private String notas;
    @Column(name = "creado_por", nullable = false, length = 150)
    private String creadoPor;
    @Column(name = "actualizado_por", nullable = false, length = 150)
    private String actualizadoPor;
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc") @Builder.Default
    private List<PedidoVentaDetalleModel> detalles = new ArrayList<>();

    @PrePersist void prePersist() { var now = LocalDateTime.now(); fechaRegistro = now; fechaActualizacion = now; }
    @PreUpdate void preUpdate() { fechaActualizacion = LocalDateTime.now(); }

    public void agregarDetalle(PedidoVentaDetalleModel detalle) {
        detalle.setPedido(this);
        detalles.add(detalle);
    }
}
