package com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorAbastecimientoDTO {
    private Long id;
    private String nombre;
    private BigDecimal calificacion;
    private Integer numeroCompras;
    private LocalDate ultimaCompra;
    private Double costoUnitario;
    private Double factorConversion;
    private Long unidadCompraId;
    private String unidadCompraSimbolo;
    private Double puntaje;
}
