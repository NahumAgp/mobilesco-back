package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleSalidaInsumoResponseDTO {
    private Long id;
    private Long salidaInsumoId;
    private Long insumoId;
    private String insumoNombre;
    private String insumoUnidad;
    private Double cantidad;
    private Double stockAnterior;
    private Double stockNuevo;
    private Double costoUnitario;
    private Double costoTotal;
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
