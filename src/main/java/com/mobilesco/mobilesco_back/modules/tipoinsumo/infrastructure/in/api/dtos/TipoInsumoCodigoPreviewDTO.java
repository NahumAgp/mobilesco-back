package com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoInsumoCodigoPreviewDTO {

    private String codigo;
    private Boolean disponible;
    private String mensaje;
}
