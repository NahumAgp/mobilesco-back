package com.mobilesco.mobilesco_back.modules.costoindirecto.domain.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cif_configuracion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CifConfiguracionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80)
    @Builder.Default
    private String nombre = "GLOBAL";

    @Column(name = "dias_laborables_mes", nullable = false)
    @Builder.Default
    private Double diasLaborablesMes = 22.0;

    @Column(name = "horas_por_dia", nullable = false)
    @Builder.Default
    private Double horasPorDia = 8.0;

    @Column(name = "turnos", nullable = false)
    @Builder.Default
    private Double turnos = 1.0;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public Double calcularMinutosProductivosMes() {
        return nz(diasLaborablesMes) * nz(horasPorDia) * nz(turnos) * 60.0;
    }

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

    private double nz(Double value) {
        return value == null ? 0.0 : value;
    }
}
