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
public class InsumoResponseDTO {
    private Long id;
    private String codigo;
    private String codigoBarras;
    private String nombre;
    private String descripcion;

    private Long unidadMedidaId;
    private String unidadMedidaNombre;
    private String unidadMedidaSimbolo;

    private Double stockActual;
    private Double stockMinimo;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    private String ubicacion;
    private String fila;
    private String columna;
    private Double ultimoCostoCompra;
    private Double costoPromedio;
    private Double costoCotizacion;
    private Boolean puedeEliminar;

    @JsonProperty("costo_cotizar")
    public Double getCostoCotizar() {
        return costoCotizacion;
    }
}
