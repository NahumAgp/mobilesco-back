package com.mobilesco.mobilesco_back.modules.productoterminado.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.OrdenProduccionModel;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.PedidoVentaModel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movimiento_producto_terminado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimientoProductoTerminadoModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "inventario_id", nullable = false)
    private InventarioProductoTerminadoModel inventario;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private TipoMovimientoProductoTerminado tipo;
    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidad;
    @Column(name = "existencia_anterior", nullable = false, precision = 14, scale = 3)
    private BigDecimal existenciaAnterior;
    @Column(name = "existencia_nueva", nullable = false, precision = 14, scale = 3)
    private BigDecimal existenciaNueva;
    @ManyToOne @JoinColumn(name = "orden_produccion_id")
    private OrdenProduccionModel ordenProduccion;
    @ManyToOne @JoinColumn(name = "pedido_venta_id")
    private PedidoVentaModel pedidoVenta;
    @Column(name = "referencia_id")
    private Long referenciaId;
    @Column(length = 500)
    private String observaciones;
    @Column(nullable = false, length = 150)
    private String usuario;
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
    @PrePersist void prePersist() { fechaRegistro = LocalDateTime.now(); }
}
