package com.mobilesco.mobilesco_back.modules.producto.domain.models;

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

import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.persistence.CostoDecimalConverter;
import com.mobilesco.mobilesco_back.modules.shared.application.costos.CostoDecimal;
import jakarta.persistence.Convert;

@Entity
@Table(
    name = "producto_operacion",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_producto_operacion", columnNames = {"producto_id", "operacion_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoOperacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_prodop_producto"))
    private ProductoModel producto;

    @ManyToOne
    @JoinColumn(name = "operacion_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_prodop_operacion"))
    private OperacionModel operacion;

    @Column
    private Integer cantidad;  // Veces que se repite

    @Column(name = "tiempo_total")
    private Double tiempoTotal;  // cantidad * operacion.tiempoOperacion

    @Convert(converter = CostoDecimalConverter.class)
    @Column(name = "importe_actividad", precision = 18, scale = 6)
    private Double importeActividad;  // tiempoTotal * operacion.costoMinuto

    @Column(nullable = false)
    private Integer orden;  // Secuencia (1,2,3...)

    @Column(length = 500)
    private String observaciones;

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
        calcularTotales();
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
        calcularTotales();
    }

    // DESPUÉS (sí funciona)
    public void calcularTotales() {  // 👈 CAMBIAR A PUBLIC
        if (operacion != null && cantidad != null) {
            this.tiempoTotal = getTiempoTotalVigente();
            this.importeActividad = getImporteActividadVigente();
        } else {
            this.tiempoTotal = null;
            this.importeActividad = null;
        }
    }

    /** El costo vigente se deriva de la operación, sin depender del importe persistido. */
    public double getTiempoTotalVigente() {
        if (operacion == null || cantidad == null || operacion.getTiempoOperacion() == null) return 0.0;
        return CostoDecimal.de(cantidad).multiply(CostoDecimal.de(operacion.getTiempoOperacion())).doubleValue();
    }

    public double getImporteActividadVigente() {
        if (operacion == null || operacion.getCostoMinuto() == null) return 0.0;
        return CostoDecimal.de(getTiempoTotalVigente())
                .multiply(CostoDecimal.de(operacion.getCostoMinuto())).doubleValue();
    }
}
