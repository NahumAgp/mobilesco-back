package com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoRequisicionAlmacen {
    ENVIADA("Enviada"),
    EN_REVISION("En revisión"),
    AUTORIZADA("Autorizada"),
    RECHAZADA("Rechazada"),
    CANCELADA("Cancelada");

    private final String etiqueta;
}
