package com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.OrdenProduccionModel;

@Entity
@Table(name = "salida_insumo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalidaInsumoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_salida", nullable = false, length = 20)
    @Builder.Default
    private String tipoSalida = "DIRECTA";

    @Column(name = "orden_produccion", length = 100)
    private String ordenProduccion;

    @ManyToOne
    @JoinColumn(name = "orden_produccion_id")
    private OrdenProduccionModel ordenProduccionModel;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDateTime fechaSalida;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "cantidad_total")
    private Double cantidadTotal;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "usuario", length = 100)
    private String usuario;

    @Column(name = "responsable", length = 150)
    private String responsable;

    @Column(name = "area", length = 120)
    private String area;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "salidaInsumo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleSalidaInsumoModel> detalles = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        fechaRegistro = now;
        fechaActualizacion = now;
        if (fechaSalida == null) {
            fechaSalida = now;
        }
        if (cantidadTotal == null) {
            cantidadTotal = 0.0;
        }
        if (tipoSalida == null || tipoSalida.isBlank()) {
            tipoSalida = "DIRECTA";
        }
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
