package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalidaInsumoResponseDTO {
    private Long id;
    private String tipoSalida;
    private String ordenProduccion;
    private Long ordenProduccionId;
    private LocalDateTime fechaSalida;
    private String observaciones;
    private String responsable;
    private String area;
    private Double cantidadTotal;
    private Boolean activo;
    private String usuario;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private List<DetalleSalidaInsumoResponseDTO> detalles;
}
