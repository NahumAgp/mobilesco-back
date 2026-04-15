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
public class DistribucionPorProductoDTO {
    private Long productoId;
    private String productoSku;
    private String productoNombre;
    private Double montoAsignado;
    private Double porcentaje;
    private List<DetalleCostoDTO> detalles;
}