package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CotizacionResponseDTO {
    private Long id;
    private String folio;
    private Long clienteId;
    private String clienteNombre;
    private String clienteWhatsapp;
    private String clienteCorreo;
    private EstadoCotizacion estado;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private BigDecimal margenPorcentaje;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal flete;
    private BigDecimal ivaPorcentaje;
    private BigDecimal subtotalCostos;
    private BigDecimal subtotalVenta;
    private BigDecimal montoDescuento;
    private BigDecimal subtotalConFlete;
    private BigDecimal montoIva;
    private BigDecimal total;
    private String notas;
    private String condiciones;
    private List<Detalle> detalles;
    private LocalDateTime fechaRegistro;

    @Data
    @Builder
    public static class Detalle {
        private Long id;
        private Long productoId;
        private String sku;
        private String nombre;
        private Integer cantidad;
        private BigDecimal costoUnitario;
        private BigDecimal precioUnitario;
        private BigDecimal importe;
    }
}
