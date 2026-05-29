package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CifConfiguracionDTO {
    private Long id;
    private String nombre;
    private Double diasLaborablesMes;
    private Double horasPorDia;
    private Double turnos;
    private Double minutosProductivosMes;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
