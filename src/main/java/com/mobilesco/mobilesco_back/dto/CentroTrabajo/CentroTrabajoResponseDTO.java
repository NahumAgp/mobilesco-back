package com.mobilesco.mobilesco_back.dto.CentroTrabajo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CentroTrabajoResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Double costoHora;
    private Double capacidadDiaria;
    private String unidadCapacidad;
    private Double horasDisponiblesDia;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}