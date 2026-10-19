package com.mobilesco.mobilesco_back.modules.costoindirecto.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Instantánea inmutable de la tasa CIF usada para una cotización. */
@Entity
@Table(name = "cif_tarifa_version")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CifTarifaVersionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer mes;

    @Column(name = "configuracion_cif_id", nullable = false)
    private Long configuracionCifId;

    @Column(name = "total_mensual", nullable = false, precision = 18, scale = 6)
    private BigDecimal totalMensual;

    @Column(name = "minutos_productivos_mes", nullable = false, precision = 18, scale = 6)
    private BigDecimal minutosProductivosMes;

    @Column(name = "tasa_minuto", nullable = false, precision = 18, scale = 8)
    private BigDecimal tasaMinuto;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        fechaRegistro = LocalDateTime.now();
    }
}
