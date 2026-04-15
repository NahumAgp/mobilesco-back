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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "distribucion_costo",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_distribucion_mensual", 
                          columnNames = {"costo_indirecto_id", "producto_id", "anio", "mes"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistribucionCostoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "costo_indirecto_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_distribucion_costo"))
    private CostoIndirectoModel costoIndirecto;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_distribucion_producto"))
    private ProductoModel producto;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "mes", nullable = false)
    private Integer mes;  // 1-12

    @Column(name = "monto_asignado", nullable = false)
    private Double montoAsignado;      // Lo que le toca a este producto

    @Column(name = "porcentaje_participacion")
    private Double porcentajeParticipacion;  // % que representa

    @Column(name = "base_calculo")
    private Double baseCalculo;          // Ej: 150 horas de MOD, 2000 kg producidos

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void prePersist() {
        fechaRegistro = LocalDateTime.now();
    }
}