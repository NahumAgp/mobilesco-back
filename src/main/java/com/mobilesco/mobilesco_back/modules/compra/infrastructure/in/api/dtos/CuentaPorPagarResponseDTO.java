package com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaPorPagarResponseDTO {
    private Long id;
    private Long compraId;
    private String compraFolio;
    private LocalDate fechaCompra;
    private Long proveedorId;
    private String proveedorRazonSocial;
    private String proveedorRfc;
    private LocalDate fechaCuenta;
    private LocalDate fechaVencimiento;
    private Double montoTotal;
    private Double montoPagado;
    private Double saldoPendiente;
    private String estado;
    private String metodoPagoCompra;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private CompraResponseDTO compra;
    private List<PagoCuentaPorPagarResponseDTO> pagos;
}
