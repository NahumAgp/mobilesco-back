package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsumoCostoResponseDTO {
    private Long id;
    private String codigo;
    private String codigoBarras;
    private String nombre;
    private Long unidadMedidaId;
    private String unidadMedidaNombre;
    private String unidadMedidaSimbolo;
    private Double ultimoCostoCompra;
    private Double costoPromedio;
    private Double costoCotizacion;
    private Boolean activo;
    private LocalDateTime fechaActualizacion;

    @JsonProperty("costo_cotizar")
    public Double getCostoCotizar() {
        return costoCotizacion;
    }
}
