package com.mobilesco.mobilesco_back.modules.nivel.domain.models;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;

import jakarta.persistence.Column;
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
        name = "nivel_insumo",
        uniqueConstraints = @UniqueConstraint(name = "uk_nivel_insumo", columnNames = {"nivel_id", "insumo_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelInsumoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nivel_id", nullable = false, foreignKey = @ForeignKey(name = "fk_nivel_insumo_nivel"))
    private NivelModel nivel;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_nivel_insumo_insumo"))
    private InsumoModel insumo;

    @Column(name = "cantidad", nullable = false)
    private Double cantidad;

    @Column(name = "desperdicio_porcentaje", nullable = false)
    @Builder.Default
    private Double desperdicioPorcentaje = 0.0;
}
