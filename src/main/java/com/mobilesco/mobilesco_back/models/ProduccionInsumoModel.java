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
@Table(name = "produccion_insumo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduccionInsumoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produccion_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_prodinsumo_produccion"))
    private ProduccionModel produccion;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_prodinsumo_insumo"))
    private InsumoModel insumo;

    @Column(name = "cantidad_teorica", nullable = false)
    private Double cantidadTeorica;  // Según BOM

    @Column(name = "cantidad_real", nullable = false)
    private Double cantidadReal;  // Lo que realmente se usó

    @Column(name = "costo_unitario", nullable = false)
    private Double costoUnitario;  // Costo del insumo en ese momento

    @Column(name = "costo_total", nullable = false)
    private Double costoTotal;  // cantidadReal * costoUnitario

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void prePersist() {
        fechaRegistro = LocalDateTime.now();
    }
}