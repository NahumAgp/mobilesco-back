package com.mobilesco.mobilesco_back.modules.cotizacion.domain.models;

import java.math.BigDecimal;

import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.models.CifTarifaVersionModel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cotizacion_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizacionDetalleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cotizacion_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cotdetalle_cotizacion"))
    private CotizacionModel cotizacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cotdetalle_producto"))
    private ProductoModel producto;

    @Column(name = "sku_snapshot", nullable = false, length = 50)
    private String skuSnapshot;

    @Column(name = "nombre_snapshot", nullable = false, length = 200)
    private String nombreSnapshot;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "utilidad_porcentaje", precision = 7, scale = 2)
    private BigDecimal utilidadPorcentaje;

    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoUnitario;

    @ManyToOne
    @JoinColumn(name = "cif_tarifa_version_id", foreignKey = @ForeignKey(name = "fk_cotdetalle_cif_tarifa_version"))
    private CifTarifaVersionModel cifTarifaVersion;

    @Column(name = "costo_insumos", precision = 14, scale = 2)
    private BigDecimal costoInsumos;

    @Column(name = "costo_operaciones", precision = 14, scale = 2)
    private BigDecimal costoOperaciones;

    @Column(name = "costo_cif", precision = 14, scale = 2)
    private BigDecimal costoCif;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;
}
