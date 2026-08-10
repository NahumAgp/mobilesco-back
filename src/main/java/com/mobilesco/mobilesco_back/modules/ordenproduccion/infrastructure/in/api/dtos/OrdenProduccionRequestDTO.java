package com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrdenProduccionRequestDTO {
    private Long clienteId;
    private LocalDate fechaInicioProgramada;
    private LocalDate fechaCompromiso;
    @Size(max=1000) private String observaciones;
    @NotEmpty @Valid private List<Partida> partidas;

    @Data
    public static class Partida {
        @NotNull private Long productoId;
        @NotNull @DecimalMin(value="1") @Digits(integer=10, fraction=0) private BigDecimal cantidad;
    }
}
