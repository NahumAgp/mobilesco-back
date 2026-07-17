/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/dtos/ProductoFichaDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoFichaDTO
 * CONTEXTO: DTO de salida (solo lectura) que agrega la ficha de un MUEBLE (modelo) con todas sus variantes.
 * NOTAS: Aditivo. Reusa ImagenResponseDTO. Agrupa colores, tamanos (niveles) y materiales distintos.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import java.util.List;

import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.dtos.ImagenResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoFichaDTO {

    private Long modeloId;
    private String codigo;            // codigo unico del mueble (modelo.codigo)
    private String nombre;            // modelo.nombre
    private String descripcion;       // modelo.descripcion
    private String urlImagenModelo;   // modelo.urlImagen
    private Long familiaId;
    private String familiaNombre;
    private Long subfamiliaId;
    private String subfamiliaNombre;

    private List<ImagenResponseDTO> imagenes;       // TODAS las imagenes de todas las variantes
    private List<ColorOpcionDTO> colores;           // distintos por id
    private List<TamanoOpcionDTO> tamanos;          // niveles distintos por id
    private List<MaterialOpcionDTO> materiales;     // distintos por id
    private List<VarianteFichaDTO> variantes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColorOpcionDTO {
        private Long id;
        private String codigo;
        private String nombre;
        private String hex;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TamanoOpcionDTO {
        private Long id;
        private String codigo;
        private String nombre;
        private String categoriaNombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialOpcionDTO {
        private Long id;
        private String codigo;
        private String nombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VarianteFichaDTO {
        private Long productoId;
        private String sku;
        private Long nivelId;
        private String nivelNombre;
        private Long colorId;
        private String colorNombre;
        private String colorHex;
        private Long materialId;
        private String materialNombre;
        private Double ancho;
        private Double alto;
        private Double fondo;
        private String dimensiones;
        private Double pesoKg;
        private Double pesoVolumetrico;
        private String caracteristicas;
        private String imagenPrincipalUrl;
        private Boolean activo;
    }
}
