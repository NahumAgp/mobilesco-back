package com.mobilesco.mobilesco_back.dto.DistribucionCosto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistribucionResumenDTO {
    private Integer anio;
    private Integer mes;
    private Double totalCostosIndirectos;
    private List<DistribucionPorProductoDTO> productos;
}