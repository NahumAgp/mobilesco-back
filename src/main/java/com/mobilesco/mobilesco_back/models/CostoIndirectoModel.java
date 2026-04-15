package com.mobilesco.mobilesco_back.models;

import java.time.LocalDateTime;

import com.mobilesco.mobilesco_back.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.enums.TipoCostoIndirecto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "costo_indirecto",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_costo_indirecto_codigo", columnNames = {"codigo"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoIndirectoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;           // "MOI-01", "ELECT-01", "RENTA-01"

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;            // "Mano de obra indirecta", "Electricidad", "Renta"

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoCostoIndirecto tipo;   // FIJO o VARIABLE

    @Enumerated(EnumType.STRING)
    @Column(name = "base_distribucion", nullable = false)
    private BaseDistribucion baseDistribucion;  // HORAS_MOD, HORAS_MAQUINA, PESO, UNIDADES

    @Column(name = "monto_mensual")
    private Double montoMensual;        // $50,000 mensuales (para costos fijos)

    @Column(name = "porcentaje_asignado")
    private Double porcentajeAsignado;   // 30% (para costos fijos por producto)

    @Column(name = "tasa_variable")
    private Double tasaVariable;         // $5 por hora, $2 por kg (para costos variables)

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