// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/VarianteController.java
// ============================================
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

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.VARIANTES)
public class VarianteController {

    private final VarianteService varianteService;

    public VarianteController(VarianteService varianteService) {
        this.varianteService = varianteService;
    }

    @PostMapping
    public ResponseEntity<VarianteResponseDTO> crear(@Valid @RequestBody VarianteCreateDTO dto) {
        VarianteResponseDTO creado = varianteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<VarianteResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(varianteService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VarianteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(varianteService.obtenerPorId(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<VarianteResponseDTO> obtenerPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(varianteService.obtenerPorSku(sku));
    }

    @GetMapping("/por-producto-base/{productoBaseId}")
    public ResponseEntity<List<VarianteResponseDTO>> obtenerPorProductoBase(@PathVariable Long productoBaseId) {
        return ResponseEntity.ok(varianteService.obtenerPorProductoBase(productoBaseId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<VarianteResponseDTO>> buscar(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long productoBaseId,
            @RequestParam(required = false) Long nivelId,
            @RequestParam(required = false) Long colorId) {
        return ResponseEntity.ok(varianteService.buscarConFiltros(sku, nombre, productoBaseId, nivelId, colorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VarianteResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody VarianteUpdateDTO dto) {
        return ResponseEntity.ok(varianteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        varianteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/completo")
    public ResponseEntity<VarianteCompletaResponseDTO> obtenerVarianteCompleta(@PathVariable Long id) {
        return ResponseEntity.ok(varianteService.obtenerVarianteCompleta(id));
    }

    @GetMapping("/sku/{sku}/completo")
    public ResponseEntity<VarianteCompletaResponseDTO> obtenerVarianteCompletaPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(varianteService.obtenerVarianteCompletaPorSku(sku));
    }
}
