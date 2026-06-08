package com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoInsumoResponseDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDate fechaRegistro;
}
