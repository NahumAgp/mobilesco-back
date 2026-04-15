package com.mobilesco.mobilesco_back.dto.CostoIndirecto;

import java.time.LocalDateTime;

import com.mobilesco.mobilesco_back.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.enums.TipoCostoIndirecto;

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