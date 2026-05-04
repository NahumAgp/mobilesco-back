package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.mobilesco.mobilesco_back.dto.Producto.ProductoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoEstructuraCostosDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoUpdateDTO;
import com.mobilesco.mobilesco_back.services.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Productos", description = "Catalogo visible de productos")
@RestController
@RequestMapping(ApiPaths.PRODUCTOS)
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Crear producto")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    @Operation(summary = "Listar productos con imagenes")
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listar() {
        return ResponseEntity.ok(productoService.obtenerTodosCompletos());
    }

    @Operation(summary = "Exportar reporte de productos a Excel")
    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        byte[] excel = productoService.generarReporteExcel(activo, busqueda, sortBy, direction);
        String filename = "productos.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProductoCompleto(id));
    }

    @Operation(summary = "Obtener producto por SKU")
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(productoService.obtenerProductoCompletoPorSku(sku));
    }

    @Operation(summary = "Obtener estructura de costos del producto")
    @GetMapping("/{id}/estructura-costos")
    public ResponseEntity<ProductoEstructuraCostosDTO> obtenerEstructuraCostos(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerEstructuraCostos(id));
    }

    @Operation(summary = "Listar productos por modelo")
    @GetMapping("/por-modelo/{modeloId}")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerPorModelo(@PathVariable Long modeloId) {
        return ResponseEntity.ok(productoService.obtenerCompletosPorModelo(modeloId));
    }

    @Operation(summary = "Buscar productos con filtros")
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscar(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long modeloId,
            @RequestParam(required = false) Long nivelId,
            @RequestParam(required = false) Long colorId) {
        return ResponseEntity.ok(productoService.buscarCompletasConFiltros(sku, nombre, modeloId, nivelId, colorId));
    }

    @Operation(summary = "Actualizar producto")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoUpdateDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @Operation(summary = "Eliminar producto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
