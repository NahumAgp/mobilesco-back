package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CotizacionRequestDTO {
    @NotNull
    private Long clienteId;
    private EstadoCotizacion estado = EstadoCotizacion.PENDIENTE;
    @Min(1)
    @Max(365)
    private Integer vigenciaDias = 15;
    @DecimalMin("0.01")
    @DecimalMax("95.00")
    private BigDecimal margenPorcentaje = new BigDecimal("35");
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;
    @DecimalMin("0.00")
    private BigDecimal flete = BigDecimal.ZERO;
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal ivaPorcentaje = new BigDecimal("16");
    private String notas;
    private String condiciones;
    @NotEmpty
    @Valid
    private List<DetalleRequest> detalles;

    @Data
    public static class DetalleRequest {
        @NotNull
        private Long productoId;
        @NotNull
        @Min(1)
        private Integer cantidad;
    }
}
