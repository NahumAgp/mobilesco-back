package com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models;

import java.math.BigDecimal;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pedido_venta_detalle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoVentaDetalleModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "pedido_venta_id", nullable = false)
    private PedidoVentaModel pedido;
    @ManyToOne(optional = false) @JoinColumn(name = "producto_id", nullable = false)
    private ProductoModel producto;
    @Column(name = "sku_snapshot", nullable = false, length = 50)
    private String skuSnapshot;
    @Column(name = "nombre_snapshot", nullable = false, length = 200)
    private String nombreSnapshot;
    @Column(name = "cantidad_solicitada", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidadSolicitada;
    @Column(name = "cantidad_producida", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidadProducida;
    @Column(name = "cantidad_reservada", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidadReservada;
    @Column(name = "cantidad_entregada", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidadEntregada;
    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;
}
