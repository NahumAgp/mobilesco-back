package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductoReclasificacionRequestDTO {

    @NotNull(message = "La línea es obligatoria")
    private Long lineaId;

    @NotNull(message = "La familia es obligatoria")
    private Long familiaId;

    private Long subfamiliaId;

    @NotNull(message = "El modelo es obligatorio")
    private Long modeloId;

    @NotNull(message = "La categoría es obligatoria")
    private Long nivelId;

    @NotNull(message = "El material es obligatorio")
    private Long materialId;

    @NotNull(message = "El color es obligatorio")
    private Long colorId;
}
