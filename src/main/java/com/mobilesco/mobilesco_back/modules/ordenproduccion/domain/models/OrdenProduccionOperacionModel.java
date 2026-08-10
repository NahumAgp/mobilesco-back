package com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="orden_produccion_operacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenProduccionOperacionModel {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="orden_detalle_id", nullable=false) private OrdenProduccionDetalleModel detalle;
    @ManyToOne(optional=false) @JoinColumn(name="operacion_id", nullable=false) private OperacionModel operacion;
    @Column(name="codigo_snapshot", nullable=false, length=50) private String codigoSnapshot;
    @Column(name="nombre_snapshot", nullable=false, length=100) private String nombreSnapshot;
    @Column(name="centro_trabajo_snapshot", nullable=false, length=150) private String centroTrabajoSnapshot;
    @Column(nullable=false) private Integer secuencia;
    @Column(name="repeticiones_planeadas", nullable=false) private Integer repeticionesPlaneadas;
    @Column(name="tiempo_planeado", nullable=false, precision=16, scale=3) private BigDecimal tiempoPlaneado;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private EstadoOperacionProduccion estado;
    @Column(name="fecha_inicio") private LocalDateTime fechaInicio;
    @Column(name="fecha_fin") private LocalDateTime fechaFin;
}
