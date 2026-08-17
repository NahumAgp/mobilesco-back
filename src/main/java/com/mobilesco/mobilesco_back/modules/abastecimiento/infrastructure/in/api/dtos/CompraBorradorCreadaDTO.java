package com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraBorradorCreadaDTO {
    private Long compraId;
    private String folio;
    private Long proveedorId;
    private String proveedorNombre;
    private String estado;
    private Integer partidas;
    private Double subtotalEstimado;
}
