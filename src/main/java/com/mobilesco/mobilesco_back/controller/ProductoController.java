package com.mobilesco.mobilesco_back.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mobilesco.mobilesco_back.dto.Producto.ProductoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoInsumoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoUpdateDTO;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionCreateDTO;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionResponseDTO;
import com.mobilesco.mobilesco_back.services.ProductoInsumoService;
import com.mobilesco.mobilesco_back.services.ProductoOperacionService;
import com.mobilesco.mobilesco_back.services.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Productos", description = "CRUD de productos y BOM (insumos y operaciones)")
@RestController
@RequestMapping(ApiPaths.PRODUCTOS)
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoInsumoService productoInsumoService;
    private final ProductoOperacionService productoOperacionService;

    // =====================================================
    // CRUD DE PRODUCTOS
    // =====================================================

    @Operation(summary = "Crear nuevo producto")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoCreateDTO dto) {
        return new ResponseEntity<>(productoService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar producto")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoUpdateDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @Operation(summary = "Obtener producto por SKU")
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(productoService.obtenerPorSku(sku));
    }

    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listar() {
        return ResponseEntity.ok(productoService.listar());
    }

    @Operation(summary = "Listar productos activos")
    @GetMapping("/activos")
    public ResponseEntity<List<ProductoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(productoService.listarActivos());
    }

    @Operation(summary = "Buscar productos por nombre")
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscar(nombre));
    }

    @Operation(summary = "Eliminar producto (desactivar)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // BOM DE INSUMOS (Materia prima)
    // =====================================================

    @Operation(summary = "Agregar insumo a producto (individual)")
    @PostMapping("/{productoId}/insumos")
    public ResponseEntity<ProductoInsumoResponseDTO> agregarInsumo(
            @PathVariable Long productoId,
            @Valid @RequestBody ProductoInsumoCreateDTO dto) {
        return new ResponseEntity<>(
                productoInsumoService.agregarInsumoAProducto(productoId, dto),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Agregar múltiples insumos a producto (masivo)")
    @PostMapping("/{productoId}/insumos/masivo")
    public ResponseEntity<List<ProductoInsumoResponseDTO>> agregarInsumosMasivo(
            @PathVariable Long productoId,
            @Valid @RequestBody List<ProductoInsumoCreateDTO> dtoList) {
        return new ResponseEntity<>(
                productoInsumoService.agregarInsumosMasivo(productoId, dtoList),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Listar insumos de un producto")
    @GetMapping("/{productoId}/insumos")
    public ResponseEntity<List<ProductoInsumoResponseDTO>> listarInsumos(
            @PathVariable Long productoId) {
        return ResponseEntity.ok(productoInsumoService.listarPorProducto(productoId));
    }

    @Operation(summary = "Eliminar insumo de producto")
    @DeleteMapping("/{productoId}/insumos/{insumoId}")
    public ResponseEntity<Void> eliminarInsumo(
            @PathVariable Long productoId,
            @PathVariable Long insumoId) {
        productoInsumoService.eliminarInsumoDeProducto(productoId, insumoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualizar cantidad de insumo en producto")
    @PutMapping("/{productoId}/insumos/{insumoId}")
    public ResponseEntity<ProductoInsumoResponseDTO> actualizarInsumo(
            @PathVariable Long productoId,
            @PathVariable Long insumoId,
            @Valid @RequestBody ProductoInsumoCreateDTO dto) {
        return ResponseEntity.ok(productoInsumoService.actualizarInsumo(productoId, insumoId, dto));
    }

    // =====================================================
// BOM DE OPERACIONES (Procesos de fabricación)
// =====================================================

@Operation(summary = "Listar operaciones de un producto")
@GetMapping("/{productoId}/operaciones")
public ResponseEntity<List<ProductoOperacionResponseDTO>> listarOperaciones(
        @PathVariable Long productoId) {
    return ResponseEntity.ok(productoOperacionService.listarPorProducto(productoId));
}

@Operation(summary = "Agregar múltiples operaciones a producto (masivo)")
@PostMapping("/{productoId}/operaciones/masivo")
public ResponseEntity<List<ProductoOperacionResponseDTO>> agregarOperacionesMasivo(
        @PathVariable Long productoId,
        @Valid @RequestBody List<ProductoOperacionCreateDTO> dtoList) {
    return new ResponseEntity<>(
            productoOperacionService.agregarOperacionesMasivo(productoId, dtoList),
            HttpStatus.CREATED);
}

@Operation(summary = "Eliminar operación de producto")
@DeleteMapping("/{productoId}/operaciones/{operacionId}")
public ResponseEntity<Void> eliminarOperacion(
        @PathVariable Long productoId,
        @PathVariable Long operacionId) {
    productoOperacionService.eliminarOperacionDeProducto(productoId, operacionId);
    return ResponseEntity.noContent().build();
}

@Operation(summary = "Reordenar operaciones de un producto")
@PutMapping("/{productoId}/operaciones/reordenar")
public ResponseEntity<Void> reordenarOperaciones(
        @PathVariable Long productoId,
        @RequestBody List<Long> operacionesIdsEnOrden) {
    productoOperacionService.reordenarOperaciones(productoId, operacionesIdsEnOrden);
    return ResponseEntity.ok().build();
}

@Operation(summary = "Calcular costo total de operaciones")
@GetMapping("/{id}/costo-operaciones")
public ResponseEntity<Double> calcularCostoOperaciones(@PathVariable Long id) {
    return ResponseEntity.ok(productoOperacionService.calcularCostoTotalOperaciones(id));
}

    // =====================================================
    // CÁLCULOS DE COSTOS
    // =====================================================

    @Operation(summary = "Calcular costo de producto (solo insumos)")
    @GetMapping("/{id}/costo")
    public ResponseEntity<Double> calcularCosto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.calcularCostoProducto(id));
    }

    @Operation(summary = "Calcular costo con desperdicio")
    @GetMapping("/{id}/costo-con-desperdicio")
    public ResponseEntity<Double> calcularCostoConDesperdicio(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.calcularCostoProductoConDesperdicio(id));
    }

    @Operation(summary = "Calcular costo total (insumos + operaciones)")
    @GetMapping("/{id}/costo-total")
    public ResponseEntity<Double> calcularCostoTotal(@PathVariable Long id) {
        double costoInsumos = productoService.calcularCostoProductoConDesperdicio(id);
        double costoOperaciones = productoOperacionService.calcularCostoTotalOperaciones(id);
        return ResponseEntity.ok(costoInsumos + costoOperaciones);
    }

    @Operation(summary = "Obtener estructura completa de costos")
    @GetMapping("/{id}/estructura-costos")
    public ResponseEntity<Map<String, Object>> obtenerEstructuraCostos(@PathVariable Long id) {
        Map<String, Object> resultado = new HashMap<>();
        
        resultado.put("productoId", id);
        resultado.put("insumos", productoInsumoService.listarPorProducto(id));
        resultado.put("operaciones", productoOperacionService.listarPorProducto(id));
        resultado.put("costoInsumos", productoService.calcularCostoProductoConDesperdicio(id));
        resultado.put("costoOperaciones", productoOperacionService.calcularCostoTotalOperaciones(id));
        resultado.put("costoTotal", 
            productoService.calcularCostoProductoConDesperdicio(id) + 
            productoOperacionService.calcularCostoTotalOperaciones(id));
        
        return ResponseEntity.ok(resultado);
    }
}