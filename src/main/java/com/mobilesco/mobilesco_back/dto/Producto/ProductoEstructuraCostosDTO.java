package com.mobilesco.mobilesco_back.dto.Producto;

import java.util.List;

import com.mobilesco.mobilesco_back.dto.DistribucionCosto.DistribucionCostoResponseDTO;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoEstructuraCostosDTO {
    private Long productoId;
    private String productoSku;
    private String productoNombre;

    private Double costoInsumosBase;
    private Double costoInsumosConDesperdicio;
    private Double costoOperaciones;
    private Double costoPrimo;
    private Double costoCif;
    private Double costoTotal;

    private Integer anioCif;
    private Integer mesCif;

    private List<ProductoInsumoResponseDTO> insumos;
    private List<ProductoOperacionResponseDTO> operaciones;
    private List<DistribucionCostoResponseDTO> costosIndirectos;
}
