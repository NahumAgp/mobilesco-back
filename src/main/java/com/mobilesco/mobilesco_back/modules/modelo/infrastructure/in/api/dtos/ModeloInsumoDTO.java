package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeloInsumoDTO {
    @JsonAlias({"insumoId", "insumo_id"})
    private Long id;
    private String codigo;
    private String nombre;
    private String unidadMedida;
    @JsonProperty("cantidad")
    private Double cantidad;
    private Boolean activo;
}
