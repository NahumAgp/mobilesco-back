package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SincronizacionMedidasVariantesResponseDTO {
    private Long modeloId;
    private Long nivelId;
    private String nivelNombre;
    private Long materialId;
    private Integer productosActualizados;
}
