package com.mobilesco.mobilesco_back.dto.SalidaInsumo;

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
    private String ordenProduccion;
    private LocalDateTime fechaSalida;
    private String observaciones;
    private Double cantidadTotal;
    private Boolean activo;
    private String usuario;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private List<DetalleSalidaInsumoResponseDTO> detalles;
}
