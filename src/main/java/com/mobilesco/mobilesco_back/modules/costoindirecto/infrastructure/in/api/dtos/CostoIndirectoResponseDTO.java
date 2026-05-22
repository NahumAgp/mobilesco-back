package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.TipoCostoIndirecto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoIndirectoResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoCostoIndirecto tipo;
    private BaseDistribucion baseDistribucion;
    private Double montoMensual;
    private Double porcentajeAsignado;
    private Double tasaVariable;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}