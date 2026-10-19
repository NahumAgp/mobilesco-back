package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos;

import jakarta.validation.constraints.Size;

public record CotizacionFleteRequestDTO(
        @Size(max = 500) String destino,
        Double latitud,
        Double longitud) {
}
