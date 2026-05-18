/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/controllers/ProductoBomController.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoBomController
 * CONTEXTO: Controlador REST BOM para insumos y operaciones del modulo Producto.
 * NOTAS: Coordina endpoints de composicion de producto.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionCreateDTO;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.services.ProductoInsumoService;
import com.mobilesco.mobilesco_back.services.ProductoOperacionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.PRODUCTOS + "/{productoId}")
public class ProductoBomController {

    private final ProductoInsumoService productoInsumoService;
    private final ProductoOperacionService productoOperacionService;

    public ProductoBomController(ProductoInsumoService productoInsumoService,
                                 ProductoOperacionService productoOperacionService) {
        this.productoInsumoService = productoInsumoService;
        this.productoOperacionService = productoOperacionService;
    }

    @GetMapping("/insumos")
    public ResponseEntity<List<ProductoInsumoResponseDTO>> listarInsumos(@PathVariable Long productoId) {
        return ResponseEntity.ok(productoInsumoService.listarPorProducto(productoId));
    }

    @PostMapping("/insumos")
    public ResponseEntity<ProductoInsumoResponseDTO> agregarInsumo(
            @PathVariable Long productoId,
            @Valid @RequestBody ProductoInsumoCreateDTO dto) {
        return ResponseEntity.ok(productoInsumoService.agregarInsumoAProducto(productoId, dto));
    }

    @PostMapping("/insumos/masivo")
    public ResponseEntity<List<ProductoInsumoResponseDTO>> agregarInsumosMasivo(
            @PathVariable Long productoId,
            @RequestBody List<@Valid ProductoInsumoCreateDTO> insumos) {
        return ResponseEntity.ok(productoInsumoService.agregarInsumosMasivo(productoId, insumos));
    }

    @PutMapping("/insumos/{insumoId}")
    public ResponseEntity<ProductoInsumoResponseDTO> actualizarInsumo(
            @PathVariable Long productoId,
            @PathVariable Long insumoId,
            @Valid @RequestBody ProductoInsumoCreateDTO dto) {
        return ResponseEntity.ok(productoInsumoService.actualizarInsumo(productoId, insumoId, dto));
    }

    @DeleteMapping("/insumos/{insumoId}")
    public ResponseEntity<Void> eliminarInsumo(
            @PathVariable Long productoId,
            @PathVariable Long insumoId) {
        productoInsumoService.eliminarInsumoDeProducto(productoId, insumoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/operaciones")
    public ResponseEntity<List<ProductoOperacionResponseDTO>> listarOperaciones(@PathVariable Long productoId) {
        return ResponseEntity.ok(productoOperacionService.listarPorProducto(productoId));
    }

    @PostMapping("/operaciones/masivo")
    public ResponseEntity<List<ProductoOperacionResponseDTO>> agregarOperacionesMasivo(
            @PathVariable Long productoId,
            @RequestBody List<@Valid ProductoOperacionCreateDTO> operaciones) {
        return ResponseEntity.ok(productoOperacionService.agregarOperacionesMasivo(productoId, operaciones));
    }

    @DeleteMapping("/operaciones/{operacionId}")
    public ResponseEntity<Void> eliminarOperacion(
            @PathVariable Long productoId,
            @PathVariable Long operacionId) {
        productoOperacionService.eliminarOperacionDeProducto(productoId, operacionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/operaciones/reordenar")
    public ResponseEntity<Void> reordenarOperaciones(
            @PathVariable Long productoId,
            @RequestBody List<Long> operacionesIds) {
        productoOperacionService.reordenarOperaciones(productoId, operacionesIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/costo-operaciones")
    public ResponseEntity<Double> calcularCostoOperaciones(@PathVariable Long productoId) {
        return ResponseEntity.ok(productoOperacionService.calcularCostoTotalOperaciones(productoId));
    }
}
