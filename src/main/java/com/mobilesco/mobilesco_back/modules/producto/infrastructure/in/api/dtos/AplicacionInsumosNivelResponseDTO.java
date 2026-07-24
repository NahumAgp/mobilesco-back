package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AplicacionInsumosNivelResponseDTO {
    private Long modeloId;
    private Long nivelId;
    private String nivelNombre;
    private Integer productosActualizados;
    private List<String> productosSku;
}
