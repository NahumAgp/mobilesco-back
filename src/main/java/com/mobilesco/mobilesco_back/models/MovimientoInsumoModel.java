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
@Table(name = "kardex")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInsumoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_kardex_insumo"))
    private InsumoModel insumo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "tipo", nullable = false, length = 10) // ENTRADA / SALIDA
    private String tipo;

    @Column(name = "concepto", nullable = false, length = 30) // COMPRA, PRODUCCION, AJUSTE, DEVOLUCION
    private String concepto;

    @Column(name = "cantidad", nullable = false)
    private Double cantidad;

    @Column(name = "costo_unitario", nullable = false)
    private Double costoUnitario;

    @Column(name = "costo_total", nullable = false)
    private Double costoTotal;

    @Column(name = "documento", length = 50) // N° Factura, N° Orden, etc.
    private String documento;

    @Column(name = "referencia", length = 100) // ID de compra, ID de producción, etc.
    private String referencia;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Column(name = "stock_anterior", nullable = false)
    private Double stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private Double stockNuevo;

    @Column(name = "usuario", length = 50)
    private String usuario;

    // Relaciones opcionales (para trazabilidad)
    @Column(name = "compra_id")
    private Long compraId;

    @Column(name = "produccion_id")
    private Long produccionId;

    @Column(name = "ajuste_id")
    private Long ajusteId;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void prePersist() {
        fechaRegistro = LocalDateTime.now();
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}