package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductoSkuValidacionResponseDTO {

    private int total;
    private int correctos;
    private int inconsistentes;
    private int corregibles;
    private int bloqueados;
    private int actualizados;
    private List<DetalleSkuDTO> detalles;

    @Getter
    @Builder
    public static class DetalleSkuDTO {
        private Long productoId;
        private String productoNombre;
        private String skuActual;
        private String skuEsperado;
        private String estado;
        private String motivo;
    }
}
