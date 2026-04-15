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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produccion_tiempo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduccionTiempoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produccion_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_prodtiempo_produccion"))
    private ProduccionModel produccion;

    @ManyToOne
    @JoinColumn(name = "operacion_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_prodtiempo_operacion"))
    private OperacionModel operacion;

    @ManyToOne
    @JoinColumn(name = "centro_trabajo_id",
                foreignKey = @ForeignKey(name = "fk_prodtiempo_centro"))
    private CentroTrabajoModel centroTrabajo;

    @Column(name = "minutos_teoricos", nullable = false)
    private Double minutosTeoricos;  // Según BOM

    @Column(name = "minutos_reales", nullable = false)
    private Double minutosReales;  // Lo que realmente tomó

    @Column(name = "costo_minuto", nullable = false)
    private Double costoMinuto;  // Costo de la operación en ese momento

    @Column(name = "costo_total", nullable = false)
    private Double costoTotal;  // minutosReales * costoMinuto

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void prePersist() {
        fechaRegistro = LocalDateTime.now();
    }
}