package com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "requisicion_almacen_detalle",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_requisicion_almacen_insumo",
                columnNames = {"requisicion_id", "insumo_id"}))
@Getter
@Setter
public class RequisicionAlmacenDetalleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requisicion_id", nullable = false)
    private RequisicionAlmacenModel requisicion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private InsumoModel insumo;

    @Column(name = "insumo_codigo", nullable = false, length = 150)
    private String insumoCodigo;

    @Column(name = "insumo_nombre", nullable = false, length = 150)
    private String insumoNombre;

    @Column(name = "unidad_simbolo", length = 30)
    private String unidadSimbolo;

    @Column(name = "cantidad_solicitada", nullable = false)
    private Double cantidadSolicitada;

    @Column(name = "stock_actual_snapshot", nullable = false)
    private Double stockActualSnapshot;

    @Column(name = "stock_minimo_snapshot")
    private Double stockMinimoSnapshot;

    @Column(name = "origen_sugerencia", nullable = false)
    private Boolean origenSugerencia;

    @Column(length = 500)
    private String observaciones;
}
