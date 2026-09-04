package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeloMedidasDTO {
    private Double ancho;
    private Double alto;
    private Double fondo;
    private String dimensiones;
    @JsonAlias({"pesoKg", "peso_kg"})
    private Double pesoKg;
    @JsonAlias({"pesoVolumetrico", "peso_volumetrico"})
    private Double pesoVolumetrico;
}
