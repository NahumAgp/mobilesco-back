package com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PagoCuentaPorPagarCreateDTO {

    private LocalDate fechaPago;

    @NotNull(message = "El monto del pago es obligatorio")
    @Positive(message = "El monto del pago debe ser mayor a 0")
    private Double monto;

    @Size(max = 60, message = "El metodo de pago no puede exceder 60 caracteres")
    private String metodoPago;

    @Size(max = 120, message = "La referencia no puede exceder 120 caracteres")
    private String referencia;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
