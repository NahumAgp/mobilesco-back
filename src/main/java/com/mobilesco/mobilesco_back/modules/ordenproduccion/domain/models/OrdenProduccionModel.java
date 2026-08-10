package com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models;

import java.time.*;
import java.util.*;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.CotizacionModel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orden_produccion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenProduccionModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 30) private String folio;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private OrigenOrdenProduccion origen;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EstadoOrdenProduccion estado;
    @ManyToOne @JoinColumn(name = "cotizacion_id", unique = true) private CotizacionModel cotizacion;
    @ManyToOne @JoinColumn(name = "cliente_id") private ClienteModel cliente;
    @Column(name = "fecha_inicio_programada") private LocalDate fechaInicioProgramada;
    @Column(name = "fecha_compromiso") private LocalDate fechaCompromiso;
    @Column(length = 1000) private String observaciones;
    @Column(name = "motivo_cancelacion", length = 1000) private String motivoCancelacion;
    @Column(name = "creado_por", nullable = false, length = 150) private String creadoPor;
    @Column(name = "actualizado_por", nullable = false, length = 150) private String actualizadoPor;
    @Column(name = "fecha_registro", nullable = false, updatable = false) private LocalDateTime fechaRegistro;
    @Column(name = "fecha_actualizacion", nullable = false) private LocalDateTime fechaActualizacion;
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true) @OrderBy("id asc") @Builder.Default
    private List<OrdenProduccionDetalleModel> detalles = new ArrayList<>();
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true) @OrderBy("id asc") @Builder.Default
    private List<OrdenProduccionInsumoModel> insumos = new ArrayList<>();
    @PrePersist void prePersist(){ var now=LocalDateTime.now(); fechaRegistro=now; fechaActualizacion=now; }
    @PreUpdate void preUpdate(){ fechaActualizacion=LocalDateTime.now(); }
}
