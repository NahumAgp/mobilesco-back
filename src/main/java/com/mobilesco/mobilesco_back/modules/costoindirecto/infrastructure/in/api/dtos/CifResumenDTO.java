package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CifResumenDTO {
    private Long configuracionId;
    private Double totalMensual;
    private Double minutosProductivosMes;
    private Double costoMinuto;
    private Long conceptosActivos;
}
