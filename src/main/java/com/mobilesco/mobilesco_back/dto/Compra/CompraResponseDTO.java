package com.mobilesco.mobilesco_back.dto.Compra;

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
public class CompraResponseDTO {
    private Long id;
    private String folio;
    private LocalDate fechaCompra;
    private LocalDate fechaRecepcion;
    
    // 🔴 Datos del proveedor
    private Long proveedorId;
    private String proveedorRazonSocial;
    private String proveedorRfc;
    private String proveedorNombreCompleto;  // nombre + apellidos
    
    private String tipoDocumento;
    private String numeroDocumento;
    private Double subtotal;
    private Double impuesto;
    private Double total;
    private String observaciones;
    private String estado;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    
    private List<DetalleCompraResponseDTO> detalles;
}