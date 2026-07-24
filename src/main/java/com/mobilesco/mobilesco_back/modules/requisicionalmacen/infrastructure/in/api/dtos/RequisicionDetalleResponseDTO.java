package com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequisicionDetalleResponseDTO {
    private Long id;
    private Long insumoId;
    private String insumoCodigo;
    private String insumoNombre;
    private String unidadSimbolo;
    private Double cantidadSolicitada;
    private Double stockActualSnapshot;
    private Double stockMinimoSnapshot;
    private Boolean origenSugerencia;
    private String observaciones;
}
