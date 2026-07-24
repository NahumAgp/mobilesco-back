package com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequisicionPartidaRequestDTO {

    @NotNull(message = "El insumo es obligatorio")
    private Long insumoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad solicitada debe ser mayor a cero")
    private Double cantidadSolicitada;

    private Boolean origenSugerencia = false;

    @Size(max = 500)
    private String observaciones;
}
