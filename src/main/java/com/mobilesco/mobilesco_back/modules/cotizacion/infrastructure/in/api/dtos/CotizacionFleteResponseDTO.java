package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos;

import java.math.BigDecimal;

public record CotizacionFleteResponseDTO(
        String origen,
        String destino,
        BigDecimal distanciaKm,
        long duracionSegundos,
        String polilinea,
        BigDecimal hastaKm,
        BigDecimal fleteSugerido) {
}
