package com.mobilesco.mobilesco_back.modules.insumo.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class InsumoModelCostosTest {
    @Test
    void nuevaCompraConDesviacionImportanteExigeRevisarCosto() {
        LocalDateTime fecha = LocalDateTime.now();
        InsumoModel insumo = InsumoModel.builder()
                .costoCotizacion(100.0).fechaCostoCotizacion(fecha).build();

        insumo.invalidarRevisionSiCompraDifiere(110.0);
        assertEquals(fecha, insumo.getFechaCostoCotizacion());
        insumo.invalidarRevisionSiCompraDifiere(120.0);
        assertNull(insumo.getFechaCostoCotizacion());
    }
}
