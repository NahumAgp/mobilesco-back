package com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data @Builder
public class PedidoVentaResponseDTO {
    private Long id;
    private String folio;
    private String estado;
    private Long cotizacionId;
    private String cotizacionFolio;
    private Long clienteId;
    private String clienteNombre;
    private LocalDate fechaPedido;
    private LocalDate fechaCompromiso;
    private BigDecimal total;
    private String notas;
    private String creadoPor;
    private String actualizadoPor;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private List<Detalle> detalles;

    @Data @Builder
    public static class Detalle {
        private Long id;
        private Long productoId;
        private String sku;
        private String nombre;
        private BigDecimal cantidadSolicitada;
        private BigDecimal cantidadProducida;
        private BigDecimal cantidadReservada;
        private BigDecimal cantidadEntregada;
        private BigDecimal pendienteProduccion;
        private BigDecimal precioUnitario;
        private BigDecimal importe;
    }
}
