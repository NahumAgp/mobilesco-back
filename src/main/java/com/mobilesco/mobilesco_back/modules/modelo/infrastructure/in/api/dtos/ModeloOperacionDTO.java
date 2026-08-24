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
public class ModeloOperacionDTO {
    @JsonAlias({"operacionId", "operacion_id"})
    private Long id;
    private String codigo;
    private String nombre;
    private String centroTrabajoNombre;
    @JsonProperty("cantidad")
    private Integer cantidad;
    private Integer orden;
    private Boolean activo;
}
