package com.mobilesco.mobilesco_back.modules.insumo.domain.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "insumo_componente", uniqueConstraints = @UniqueConstraint(columnNames = {"conjunto_id", "insumo_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ComponenteInsumoModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "conjunto_id", nullable = false)
    private InsumoModel conjunto;
    @ManyToOne(optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private InsumoModel insumo;
    @Column(nullable = false)
    private Double cantidad;
}
