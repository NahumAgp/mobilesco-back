package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record ConjuntoInsumoDTO(
        Long id,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull Long unidadMedidaId,
        String unidadMedida,
        Double costoCotizacion,
        Long version,
        @NotEmpty List<@NotNull @Valid Componente> componentes) {
    public record Componente(@NotNull Long insumoId, String nombre, String unidadMedida,
            @NotNull @Positive Double cantidad, Double costoCotizacion) {}
}
