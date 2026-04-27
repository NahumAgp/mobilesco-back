package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.variante.VarianteCompletaResponseDTO;
import com.mobilesco.mobilesco_back.dto.variante.VarianteCreateDTO;
import com.mobilesco.mobilesco_back.dto.variante.VarianteResponseDTO;
import com.mobilesco.mobilesco_back.dto.variante.VarianteUpdateDTO;
import com.mobilesco.mobilesco_back.services.VarianteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Productos", description = "Catalogo visible de productos basado en variantes")
@RestController
@RequestMapping(ApiPaths.PRODUCTOS)
@RequiredArgsConstructor
public class ProductoController {

    private final VarianteService varianteService;

    @Operation(summary = "Crear producto visible como variante")
    @PostMapping
    public ResponseEntity<VarianteResponseDTO> crear(@Valid @RequestBody VarianteCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(varianteService.crear(dto));
    }

    @Operation(summary = "Listar productos visibles con imagenes")
    @GetMapping
    public ResponseEntity<List<VarianteCompletaResponseDTO>> listar() {
        return ResponseEntity.ok(varianteService.obtenerTodosCompletos());
    }

    @Operation(summary = "Obtener producto visible por ID")
    @GetMapping("/{id}")
    public ResponseEntity<VarianteCompletaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(varianteService.obtenerVarianteCompleta(id));
    }

    @Operation(summary = "Obtener producto visible por SKU")
    @GetMapping("/sku/{sku}")
    public ResponseEntity<VarianteCompletaResponseDTO> obtenerPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(varianteService.obtenerVarianteCompletaPorSku(sku));
    }

    @Operation(summary = "Listar productos visibles por modelo")
    @GetMapping("/por-producto-base/{productoBaseId}")
    public ResponseEntity<List<VarianteCompletaResponseDTO>> obtenerPorProductoBase(@PathVariable Long productoBaseId) {
        return ResponseEntity.ok(varianteService.obtenerCompletosPorProductoBase(productoBaseId));
    }

    @Operation(summary = "Buscar productos visibles con filtros")
    @GetMapping("/buscar")
    public ResponseEntity<List<VarianteCompletaResponseDTO>> buscar(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long productoBaseId,
            @RequestParam(required = false) Long nivelId,
            @RequestParam(required = false) Long colorId) {
        return ResponseEntity.ok(varianteService.buscarCompletasConFiltros(sku, nombre, productoBaseId, nivelId, colorId));
    }

    @Operation(summary = "Actualizar producto visible")
    @PutMapping("/{id}")
    public ResponseEntity<VarianteResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody VarianteUpdateDTO dto) {
        return ResponseEntity.ok(varianteService.actualizar(id, dto));
    }

    @Operation(summary = "Eliminar producto visible")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        varianteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
