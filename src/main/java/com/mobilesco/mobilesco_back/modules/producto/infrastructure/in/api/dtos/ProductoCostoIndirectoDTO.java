package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCostoIndirectoDTO {
    private Long id;
    private String costoIndirectoCodigo;
    private String costoIndirectoNombre;
    private String tipo;
    private String periodicidad;
    private Double monto;
    private Double montoMensual;
    private Double costoMinuto;
    private Double porcentajeParticipacion;
    private String baseCalculo;
    private Double montoAsignado;
}
