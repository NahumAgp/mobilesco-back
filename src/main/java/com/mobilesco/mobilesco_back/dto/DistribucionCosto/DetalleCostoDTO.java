package com.mobilesco.mobilesco_back.dto.DistribucionCosto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCostoDTO {
    private String costoCodigo;
    private String costoNombre;
    private Double monto;
    private String baseDistribucion;
}