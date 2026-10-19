package com.mobilesco.mobilesco_back.modules.cotizacion.domain.models;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "flete_tabulador")
@Getter
@Setter
public class FleteTabuladorModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hasta_km", nullable = false, precision = 10, scale = 2, unique = true)
    private BigDecimal hastaKm;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;
}
