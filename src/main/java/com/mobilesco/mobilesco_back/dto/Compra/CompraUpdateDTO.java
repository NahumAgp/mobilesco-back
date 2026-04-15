package com.mobilesco.mobilesco_back.dto.Compra;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CompraUpdateDTO {
    
    private String folio;
    private LocalDate fechaCompra;
    private LocalDate fechaRecepcion;
    private Long proveedorId;
    private String tipoDocumento;
    private String numeroDocumento;
    private Double subtotal;
    private Double impuesto;
    private Double total;
    private String observaciones;
    private String estado;
    private Boolean activo;
}