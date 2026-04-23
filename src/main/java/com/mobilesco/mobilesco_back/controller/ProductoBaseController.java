// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/ProductoBaseController.java
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

import com.mobilesco.mobilesco_back.dto.productobase.ProductoBaseCreateDTO;
import com.mobilesco.mobilesco_back.dto.productobase.ProductoBaseResponseDTO;
import com.mobilesco.mobilesco_back.dto.productobase.ProductoBaseUpdateDTO;
import com.mobilesco.mobilesco_back.services.ProductoBaseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos-base")
public class ProductoBaseController {

    private final ProductoBaseService productoBaseService;

    public ProductoBaseController(ProductoBaseService productoBaseService) {
        this.productoBaseService = productoBaseService;
    }

    @PostMapping
    public ResponseEntity<ProductoBaseResponseDTO> crear(@Valid @RequestBody ProductoBaseCreateDTO dto) {
        ProductoBaseResponseDTO creado = productoBaseService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<ProductoBaseResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(productoBaseService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoBaseResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoBaseService.obtenerPorId(id));
    }

    @GetMapping("/por-familia/{familiaId}")
    public ResponseEntity<List<ProductoBaseResponseDTO>> obtenerPorFamilia(@PathVariable Long familiaId) {
        return ResponseEntity.ok(productoBaseService.obtenerPorFamilia(familiaId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoBaseResponseDTO>> buscar(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long familiaId) {
        return ResponseEntity.ok(productoBaseService.buscarConFiltros(sku, nombre, familiaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoBaseResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoBaseUpdateDTO dto) {
        return ResponseEntity.ok(productoBaseService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoBaseService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
