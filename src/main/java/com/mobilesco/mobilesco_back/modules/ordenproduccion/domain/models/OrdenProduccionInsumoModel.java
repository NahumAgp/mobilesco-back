package com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models;

import java.math.BigDecimal;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="orden_produccion_insumo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenProduccionInsumoModel {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="orden_produccion_id", nullable=false) private OrdenProduccionModel orden;
    @ManyToOne(optional=false) @JoinColumn(name="insumo_id", nullable=false) private InsumoModel insumo;
    @Column(name="codigo_snapshot", nullable=false, length=150) private String codigoSnapshot;
    @Column(name="nombre_snapshot", nullable=false, length=150) private String nombreSnapshot;
    @Column(name="unidad_snapshot", nullable=false, length=20) private String unidadSnapshot;
    @Column(name="cantidad_requerida", nullable=false, precision=16, scale=4) private BigDecimal cantidadRequerida;
    @Column(name="cantidad_surtida", nullable=false, precision=16, scale=4) @Builder.Default private BigDecimal cantidadSurtida=BigDecimal.ZERO;
    @Column(name="cantidad_apartada", nullable=false, precision=16, scale=4) @Builder.Default private BigDecimal cantidadApartada=BigDecimal.ZERO;
}
