package com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SeleccionSugerenciaDTO {
    @NotNull(message = "El insumo es obligatorio")
    private Long insumoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double cantidad;

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;
}
