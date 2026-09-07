package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.*;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

class DespieceProductoTest {
    private InsumoModel insumo(long id, double costo) {
        return InsumoModel.builder().id(id).nombre("Insumo " + id).costoCotizacion(costo).build();
    }
    private void componente(InsumoModel conjunto, InsumoModel insumo, double cantidad) {
        conjunto.getComponentes().add(ComponenteInsumoModel.builder().conjunto(conjunto).insumo(insumo).cantidad(cantidad).build());
    }
    private ProductoInsumoModel asignar(InsumoModel insumo, double cantidad, double desperdicio) {
        return ProductoInsumoModel.builder().insumo(insumo).cantidad(cantidad).desperdicioPorcentaje(desperdicio).build();
    }
    @Test
    void multiplicaSoldaduraYSumaInsumosRepetidosDirectosYEnOtroConjunto() {
        var micro = insumo(1, 20);
        var co2 = insumo(2, 10);
        var soldadura = InsumoModel.builder().id(3L).conjunto(true).build();
        componente(soldadura, micro, 1);
        componente(soldadura, co2, .6);
        var respaldo = InsumoModel.builder().id(4L).conjunto(true).build();
        componente(respaldo, micro, 2);
        var resultado = DespieceProducto.consolidar(List.of(asignar(soldadura, 10, 10), asignar(respaldo, 1, 0), asignar(micro, 1, 0)));
        assertEquals(2, resultado.size());
        assertEquals(0, new BigDecimal("14").compareTo(resultado.get(0).cantidad()));
        assertEquals(0, new BigDecimal("6.6").compareTo(resultado.get(1).cantidad()));
        assertEquals(26, soldadura.getCostoCotizacion());
    }
    @Test
    void cambiarDespieceYCostoSeReflejaEnTodasLasAsignaciones() {
        var tubo = insumo(1, 5);
        var base = InsumoModel.builder().id(2L).conjunto(true).build();
        componente(base, tubo, 2);
        var silla = asignar(base, 1, 0);
        var banco = asignar(base, 3, 0);
        base.getComponentes().getFirst().setCantidad(4.0);
        tubo.setCostoCotizacion(6.0);
        assertEquals(24, silla.getInsumo().getCostoCotizacion());
        assertEquals(24, banco.getInsumo().getCostoCotizacion());
        assertEquals(0, new BigDecimal("4").compareTo(DespieceProducto.consolidar(List.of(silla)).getFirst().cantidad()));
        assertEquals(0, new BigDecimal("12").compareTo(DespieceProducto.consolidar(List.of(banco)).getFirst().cantidad()));
    }
    @Test
    void noPermiteStockPropioNiCostosParciales() {
        var conjunto = InsumoModel.builder().conjunto(true).build();
        componente(conjunto, insumo(1, 0), 1);
        assertNull(conjunto.getCostoCotizacion());
        assertThrows(ValidationException.class, conjunto::exigirInsumoDirecto);
        assertThrows(ValidationException.class, () -> DespieceProducto.consolidar(List.of(asignar(conjunto, Double.NaN, 0))));
    }
}
