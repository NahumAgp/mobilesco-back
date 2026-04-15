package com.mobilesco.mobilesco_back.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "centro_trabajo",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_centro_trabajo_codigo", columnNames = {"codigo"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CentroTrabajoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;           // "SIERRA-01", "INYEC-03", "ENSAM-01"

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;            // "Sierra industrial", "Inyectora #3", "Mesa de ensamble"

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "costo_hora", nullable = false)
    private Double costoHora;          // $330 por hora (incluye energía, mantenimiento, depreciación)

    @Column(name = "capacidad_diaria")
    private Double capacidadDiaria;     // 200 cortes por día, 100 sillas por día

    @Column(name = "unidad_capacidad", length = 50)
    private String unidadCapacidad;     // "cortes", "piezas", "sillas", "horas"

    @Column(name = "horas_disponibles_dia")
    private Double horasDisponiblesDia; // 8 horas, 16 horas (doble turno)

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

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