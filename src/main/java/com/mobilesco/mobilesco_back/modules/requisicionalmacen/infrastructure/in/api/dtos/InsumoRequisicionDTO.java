package com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsumoRequisicionDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String unidadSimbolo;
    private Double stockActual;
    private Double stockMinimo;
    private Double faltanteMinimo;
    private Double cantidadSugerida;
    private Boolean bajoMinimo;
}
