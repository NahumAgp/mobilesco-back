/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/dtos/CatalogoFacetasDTO.java
 * AUTOR: Mobilesco
 * NOMBRE DE LA CLASE: CatalogoFacetasDTO
 * CONTEXTO: DTO de salida (solo lectura) con las facetas del catalogo PUBLICO: opciones de subfamilia,
 *           color y tamano disponibles con su conteo de muebles (modelos activos).
 * NOTAS: Aditivo. El conteo es de MODELOS (muebles) con al menos una variante activa que tenga la opcion,
 *        no de variantes, para que coincida con lo que el grid publico mostraria al aplicar el filtro.
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
public class CatalogoFacetasDTO {

    private List<SubfamiliaFacetaDTO> subfamilias;
    private List<ColorFacetaDTO> colores;
    private List<TamanoFacetaDTO> tamanos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubfamiliaFacetaDTO {
        private Long id;
        private String nombre;
        private Long conteo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColorFacetaDTO {
        private Long id;
        private String nombre;
        private String hex;
        private Long conteo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TamanoFacetaDTO {
        private Long id;
        private String nombre;
        private Long conteo;
    }
}
