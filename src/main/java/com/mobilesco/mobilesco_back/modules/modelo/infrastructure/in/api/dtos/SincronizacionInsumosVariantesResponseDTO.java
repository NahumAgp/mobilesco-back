package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SincronizacionInsumosVariantesResponseDTO {
    private Long modeloId;
    private Long nivelId;
    private String nivelNombre;
    private Integer productosActualizados;
    private Integer insumosAgregados;
    private Integer insumosActualizados;
    private Integer insumosEliminados;
}
