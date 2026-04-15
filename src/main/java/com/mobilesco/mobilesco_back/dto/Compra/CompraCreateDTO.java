package com.mobilesco.mobilesco_back.dto.Compra;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompraCreateDTO {
    
    private String folio;
    
    @NotNull(message = "La fecha de compra es obligatoria")
    private LocalDate fechaCompra;
    
    private LocalDate fechaRecepcion;
    
    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;
    
    @Size(max = 30)
    private String tipoDocumento;
    
    @Size(max = 50)
    private String numeroDocumento;
    
    private Double subtotal;
    private Double impuesto;
    private Double total;
    
    @Size(max = 500)
    private String observaciones;
    
    @NotNull(message = "Debe incluir al menos un detalle")
    private List<DetalleCompraCreateDTO> detalles;
}