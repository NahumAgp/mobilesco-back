package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import java.math.BigDecimal;
import java.util.*;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

public final class DespieceProducto {
    private DespieceProducto() {}
    public record Consumo(InsumoModel insumo, BigDecimal cantidad) {}

    public static List<Consumo> consolidar(List<ProductoInsumoModel> asignaciones) {
        Map<Long, Consumo> resultado = new LinkedHashMap<>();
        for (var item : asignaciones) {
            if (item.getCantidad() == null || !Double.isFinite(item.getCantidad()) || item.getCantidad() <= 0) {
                throw new ValidationException("Captura la cantidad de " + item.getInsumo().getNombre());
            }
            double desperdicio = item.getDesperdicioPorcentaje() == null ? 0 : item.getDesperdicioPorcentaje();
            if (!Double.isFinite(desperdicio) || desperdicio < 0) throw new ValidationException("Desperdicio invalido");
            BigDecimal cantidad = BigDecimal.valueOf(item.getCantidad())
                    .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(desperdicio).movePointLeft(2)));
            var insumo = item.getInsumo();
            if (insumo.isConjunto()) {
                if (insumo.getComponentes().isEmpty()) throw new ValidationException("El conjunto " + insumo.getNombre() + " no tiene despiece");
                for (var componente : insumo.getComponentes()) {
                    agregar(resultado, componente.getInsumo(), cantidad.multiply(BigDecimal.valueOf(componente.getCantidad())));
                }
            } else {
                agregar(resultado, insumo, cantidad);
            }
        }
        return new ArrayList<>(resultado.values());
    }

    private static void agregar(Map<Long, Consumo> resultado, InsumoModel insumo, BigDecimal cantidad) {
        if (insumo.isConjunto()) throw new ValidationException("El despiece debe contener insumos directos");
        resultado.compute(insumo.getId(), (id, actual) -> new Consumo(insumo,
                actual == null ? cantidad : actual.cantidad().add(cantidad)));
    }
}
