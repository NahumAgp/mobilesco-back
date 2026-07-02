/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/dtos/ModeloCatalogoPublicoDTO.java
 * AUTOR: Mobilesco
 * NOMBRE DE LA CLASE: ModeloCatalogoPublicoDTO
 * CONTEXTO: DTO de salida (solo lectura) para el listado del catalogo PUBLICO de muebles (modelos activos).
 * NOTAS: Aditivo. Version ligera para alimentar el grid del catalogo en la web publica; la ficha completa
 *        se obtiene aparte via /public/catalogo/modelo/{modeloId}/ficha (ProductoFichaDTO).
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeloCatalogoPublicoDTO {

    private Long modeloId;
    private String codigo;            // codigo unico del mueble (modelo.codigo)
    private String nombre;            // modelo.nombre
    private String descripcion;       // modelo.descripcion
    private String urlImagenModelo;   // modelo.urlImagen (ruta publica /uploads/...)
    private Long familiaId;
    private String familiaNombre;
}
