package com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="orden_produccion_avance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenProduccionAvanceModel {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="orden_detalle_id", nullable=false) private OrdenProduccionDetalleModel detalle;
    @Column(nullable=false, precision=14, scale=3) private BigDecimal cantidad;
    @Column(length=500) private String observaciones;
    @Column(nullable=false, length=150) private String usuario;
    @Column(name="fecha_registro", nullable=false, updatable=false) private LocalDateTime fechaRegistro;
    @PrePersist void prePersist(){ fechaRegistro=LocalDateTime.now(); }
}
