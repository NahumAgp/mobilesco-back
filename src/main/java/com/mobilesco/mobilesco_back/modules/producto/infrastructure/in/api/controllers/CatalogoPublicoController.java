/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/controllers/CatalogoPublicoController.java
 * AUTOR: Mobilesco
 * NOMBRE DE LA CLASE: CatalogoPublicoController
 * CONTEXTO: Controlador REST PUBLICO (sin autenticacion) que expone el catalogo de muebles para la web publica.
 * NOTAS: Solo lectura (GET). Reutiliza ProductoService. Solo devuelve modelos y variantes ACTIVOS.
 *        La whitelist de seguridad para /api/v1/public/** vive en SecurityConfig.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.producto.application.usecases.ProductoService;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.CatalogoFacetasDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ModeloCatalogoPublicoDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoFichaDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Catalogo Publico", description = "Catalogo de muebles para la web publica (sin autenticacion)")
@RestController
@RequestMapping(ApiPaths.PUBLIC_CATALOGO)
@RequiredArgsConstructor
public class CatalogoPublicoController {

    private final ProductoService productoService;

    @Operation(summary = "Listar muebles (modelos activos) del catalogo publico, con filtros opcionales")
    @GetMapping("/modelos")
    public ResponseEntity<List<ModeloCatalogoPublicoDTO>> listarModelos(
            @RequestParam(required = false) Long familiaId,
            @RequestParam(required = false) Long subfamiliaId,
            @RequestParam(required = false) Long colorId,
            @RequestParam(required = false) Long tamanoId) {
        return ResponseEntity.ok(productoService.listarCatalogoPublico(familiaId, subfamiliaId, colorId, tamanoId));
    }

    @Operation(summary = "Facetas del catalogo publico (subfamilias, colores y tamanos con conteo de muebles)")
    @GetMapping("/facetas")
    public ResponseEntity<CatalogoFacetasDTO> obtenerFacetas(
            @RequestParam(required = false) Long familiaId,
            @RequestParam(required = false) Long subfamiliaId) {
        return ResponseEntity.ok(productoService.obtenerFacetasPublicas(familiaId, subfamiliaId));
    }

    @Operation(summary = "Ficha publica del mueble (modelo) con sus variantes activas")
    @GetMapping("/modelo/{modeloId}/ficha")
    public ResponseEntity<ProductoFichaDTO> obtenerFicha(@PathVariable Long modeloId) {
        return ResponseEntity.ok(productoService.obtenerFichaPublicaPorModelo(modeloId));
    }
}
