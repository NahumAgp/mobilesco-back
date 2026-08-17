package com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprasBorradorResponseDTO {
    private Integer cantidadCompras;
    private Integer cantidadPartidas;
    private List<CompraBorradorCreadaDTO> compras;
}
