package com.mobilesco.mobilesco_back.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_salida_insumo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleSalidaInsumoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "salida_insumo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_salida_insumo"))
    private SalidaInsumoModel salidaInsumo;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_salida_insumo_insumo"))
    private InsumoModel insumo;

    @Column(nullable = false)
    private Double cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Double stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private Double stockNuevo;

    @Column(name = "costo_unitario", nullable = false)
    private Double costoUnitario;

    @Column(name = "costo_total", nullable = false)
    private Double costoTotal;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        fechaRegistro = now;
        fechaActualizacion = now;
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
