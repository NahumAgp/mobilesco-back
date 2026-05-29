package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InsumoCostoCotizacionUpdateDTO {

    @NotNull(message = "El costo de cotizacion es obligatorio")
    @Positive(message = "El costo de cotizacion debe ser mayor a 0")
    @JsonAlias("costo_cotizar")
    private Double costoCotizacion;
}
