package com.mobilesco.mobilesco_back.modules.productoterminado.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class InventarioProductoTerminadoResponseDTO {
    private Long id;
    private Long productoId;
    private String sku;
    private String nombre;
    private BigDecimal existencia;
    private BigDecimal reservado;
    private BigDecimal disponible;
    private LocalDateTime fechaActualizacion;
}
