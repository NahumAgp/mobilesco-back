package com.mobilesco.mobilesco_back.modules.cliente.domain.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoPersonaCliente {
    FISICA("Persona física"),
    MORAL("Persona moral");

    private final String etiqueta;
}
