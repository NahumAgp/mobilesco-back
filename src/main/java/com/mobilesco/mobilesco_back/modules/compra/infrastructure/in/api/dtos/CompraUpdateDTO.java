package com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CompraUpdateDTO {
    
    private String folio;
    private LocalDate fechaCompra;
    private LocalDate fechaRecepcion;
    private Long proveedorId;
    private String tipoDocumento;
    private String numeroDocumento;
    private String metodoPago;
    private Double subtotal;
    private Double impuesto;
    private Double total;
    private String observaciones;
    private String entregadoPor;
    private String estado;
    private Boolean activo;

    @Valid
    private List<DetalleCompraCreateDTO> detalles;
}
