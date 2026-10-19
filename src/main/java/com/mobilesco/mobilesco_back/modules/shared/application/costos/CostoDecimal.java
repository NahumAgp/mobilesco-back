package com.mobilesco.mobilesco_back.modules.shared.application.costos;

import java.math.BigDecimal;

public final class CostoDecimal {
    private CostoDecimal() {
    }

    public static BigDecimal de(Double valor) {
        return valor == null ? BigDecimal.ZERO : BigDecimal.valueOf(valor);
    }

    public static BigDecimal de(Integer valor) {
        return valor == null ? BigDecimal.ZERO : BigDecimal.valueOf(valor);
    }
}
