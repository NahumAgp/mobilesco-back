package com.mobilesco.mobilesco_back.modules.nivel.domain.models;

import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.persistence.CostoDecimalConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "nivel_operacion",
        uniqueConstraints = @UniqueConstraint(name = "uk_nivel_operacion_material", columnNames = {"nivel_id", "material_id", "operacion_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelOperacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nivel_id", nullable = false, foreignKey = @ForeignKey(name = "fk_nivel_operacion_nivel"))
    private NivelModel nivel;

    @ManyToOne
    @JoinColumn(name = "operacion_id", nullable = false, foreignKey = @ForeignKey(name = "fk_nivel_operacion_operacion"))
    private OperacionModel operacion;

    @ManyToOne
    @JoinColumn(name = "material_id", foreignKey = @ForeignKey(name = "fk_nivel_operacion_material"))
    private MaterialModel material;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Convert(converter = CostoDecimalConverter.class)
    @Column(name = "tiempo_minutos_inicial", precision = 18, scale = 6)
    private Double tiempoMinutosInicial;

    @Convert(converter = CostoDecimalConverter.class)
    @Column(name = "costo_minuto_inicial", precision = 18, scale = 6)
    private Double costoMinutoInicial;

    @Column(name = "costo_inicial", nullable = false)
    @Builder.Default
    private Boolean costoInicial = false;
}
