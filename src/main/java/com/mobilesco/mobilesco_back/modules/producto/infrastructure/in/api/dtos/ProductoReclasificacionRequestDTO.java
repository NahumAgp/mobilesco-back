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
}
