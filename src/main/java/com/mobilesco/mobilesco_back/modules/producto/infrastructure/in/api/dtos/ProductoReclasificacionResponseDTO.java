package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoReclasificacionResponseDTO {

    private Long productoBaseId;
    private String productoBaseNombre;
    private String rutaActual;
    private String rutaDestino;
    private int variantesAfectadas;
    private boolean permitido;
    private String motivoBloqueo;
    private List<CambioSkuDTO> cambiosSku;

    @Data
    @Builder
    public static class CambioSkuDTO {
        private Long productoId;
        private String nombre;
        private String skuAnterior;
        private String skuNuevo;
    }
}
