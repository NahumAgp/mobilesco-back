/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/dtos/ProductoEstructuraCostosDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoEstructuraCostosDTO
 * CONTEXTO: DTO de salida para estructura de costos de producto.
 * NOTAS: Agrega costos de materiales y operaciones.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import java.util.List;

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
    private Double tiempoOperacionesMinutos;
    private Double tasaCifMinuto;
    private Double cifMensual;
    private Double minutosProductivosMes;
    private Long configuracionCifId;

    private Integer anioCif;
    private Integer mesCif;

    private List<ProductoInsumoResponseDTO> insumos;
    private List<ProductoOperacionResponseDTO> operaciones;
    private List<ProductoCostoIndirectoDTO> costosIndirectos;
}
