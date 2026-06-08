package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import java.util.ArrayList;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCreacionCompletaResponseDTO {
    private ModeloResponseDTO modelo;
    private List<ProductoCreadoDTO> productos = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoCreadoDTO {
        private String clientRef;
        private ProductoResponseDTO producto;
    }
}
