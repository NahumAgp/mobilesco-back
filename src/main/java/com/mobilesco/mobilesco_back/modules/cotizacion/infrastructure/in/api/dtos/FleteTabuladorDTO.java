package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record FleteTabuladorDTO(
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal hastaKm,
        @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal precio) {
}
