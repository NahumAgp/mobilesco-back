package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.util.List;

public record CostoInicialModeloDTO(
        Long fuenteHojaId,
        Long nivelId,
        Long materialId,
        String materialNombre,
        String categoria,
        String codigoFuente,
        String archivo,
        String hoja,
        boolean costoInicialPendiente,
        BigDecimal materiaPrima,
        BigDecimal manoObra,
        BigDecimal costoPrimo,
        BigDecimal indirectos,
        BigDecimal costoFabricacion,
        BigDecimal minutosElaboracion,
        BigDecimal precioBaseSinIva,
        BigDecimal precioMenudeoSinIva,
        BigDecimal precioForaneoSinIva,
        BigDecimal precioBaseConIva,
        BigDecimal precioMenudeoConIva,
        BigDecimal precioForaneoConIva,
        List<Renglon> materiales,
        List<Renglon> operaciones) {

    public record Renglon(
            Integer fila,
            String descripcion,
            String unidad,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal importe,
            String estado,
            String motivo,
            Long insumoId,
            Long operacionId) {
    }
}
