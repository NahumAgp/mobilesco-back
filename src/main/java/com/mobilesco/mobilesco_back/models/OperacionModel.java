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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "operacion",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_operacion_codigo", columnNames = {"codigo"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "tiempo_operacion", nullable = false)
    private Double tiempoOperacion;  // Tiempo estándar en minutos por unidad

    @Column(name = "costo_hora")
    private Double costoHora;

    @Column(name = "costo_minuto")
    private Double costoMinuto;

    @ManyToOne
    @JoinColumn(name = "centro_trabajo_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_operacion_centro_trabajo"))
    private CentroTrabajoModel centroTrabajo;

    @Column(nullable = false)
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
        
        // Calcular costo por minuto si solo tenemos costo por hora
        if (costoHora != null && costoMinuto == null) {
            costoMinuto = costoHora / 60;
        }
        // Calcular costo por hora si solo tenemos costo por minuto
        if (costoMinuto != null && costoHora == null) {
            costoHora = costoMinuto * 60;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
        
        // Mantener consistencia
        if (costoHora != null && costoMinuto == null) {
            costoMinuto = costoHora / 60;
        }
        if (costoMinuto != null && costoHora == null) {
            costoHora = costoMinuto * 60;
        }
    }
}