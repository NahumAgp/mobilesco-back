package com.mobilesco.mobilesco_back.modules.shared.infrastructure.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Compatibilidad con la API Double mientras los importes se guardan como DECIMAL. */
@Converter
public class CostoDecimalConverter implements AttributeConverter<Double, BigDecimal> {
    @Override
    public BigDecimal convertToDatabaseColumn(Double valor) {
        return valor == null ? null : BigDecimal.valueOf(valor).setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    public Double convertToEntityAttribute(BigDecimal valor) {
        return valor == null ? null : valor.doubleValue();
    }
}
