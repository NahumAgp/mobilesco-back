package com.mobilesco.mobilesco_back.modules.cliente.domain.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClasificacionCliente {
    PROSPECTO("Prospecto"),
    NUEVO("Cliente nuevo"),
    RECURRENTE("Cliente recurrente"),
    DISTRIBUIDOR("Distribuidor"),
    FORANEO("Cliente foráneo"),
    PRIORITARIO("Cliente prioritario");

    private final String etiqueta;
}
