package com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models;

import java.math.BigDecimal;
import java.util.*;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="orden_produccion_detalle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenProduccionDetalleModel {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="orden_produccion_id", nullable=false) private OrdenProduccionModel orden;
    @ManyToOne(optional=false) @JoinColumn(name="producto_id", nullable=false) private ProductoModel producto;
    @Column(name="sku_snapshot", nullable=false, length=50) private String skuSnapshot;
    @Column(name="nombre_snapshot", nullable=false, length=200) private String nombreSnapshot;
    @Column(name="cantidad_planeada", nullable=false, precision=14, scale=3) private BigDecimal cantidadPlaneada;
    @Column(name="cantidad_terminada", nullable=false, precision=14, scale=3) @Builder.Default private BigDecimal cantidadTerminada=BigDecimal.ZERO;
    @OneToMany(mappedBy="detalle", cascade=CascadeType.ALL, orphanRemoval=true) @OrderBy("secuencia asc") @Builder.Default
    private List<OrdenProduccionOperacionModel> operaciones=new ArrayList<>();
    @OneToMany(mappedBy="detalle", cascade=CascadeType.ALL, orphanRemoval=true) @OrderBy("fechaRegistro desc") @Builder.Default
    private List<OrdenProduccionAvanceModel> avances=new ArrayList<>();
}
