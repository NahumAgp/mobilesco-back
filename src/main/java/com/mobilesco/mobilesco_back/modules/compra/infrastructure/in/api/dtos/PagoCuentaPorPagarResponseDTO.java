package com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoCuentaPorPagarResponseDTO {
    private Long id;
    private Long cuentaPorPagarId;
    private LocalDate fechaPago;
    private Double monto;
    private String metodoPago;
    private String referencia;
    private String observaciones;
    private String usuario;
    private LocalDateTime fechaRegistro;
}
