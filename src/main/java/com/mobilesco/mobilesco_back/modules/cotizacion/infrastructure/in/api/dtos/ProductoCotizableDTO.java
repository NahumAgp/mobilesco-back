package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoCotizableDTO {
    private Long id;
    private String sku;
    private String nombre;
    private boolean cotizable;
    private BigDecimal costoTotal;
    private List<String> faltantes;
}
