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
import com.mobilesco.mobilesco_back.dto.Insumo.InsumoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Insumo.InsumoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Insumo.InsumoUpdateDTO;
import com.mobilesco.mobilesco_back.services.InsumoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Insumos", description = "CRUD y gestión de insumos (materia prima)")
@RestController
@RequestMapping(ApiPaths.INSUMOS)
@RequiredArgsConstructor
public class InsumoController {

    private final InsumoService insumoService;

    @Operation(summary = "Crear insumo", description = "Crea un nuevo insumo")
    @PostMapping
    public ResponseEntity<InsumoResponseDTO> crear(@Valid @RequestBody InsumoCreateDTO dto) {
        return new ResponseEntity<>(insumoService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar insumo", description = "Actualiza un insumo existente")
    @PutMapping("/{id}")
    public ResponseEntity<InsumoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody InsumoUpdateDTO dto) {
        return ResponseEntity.ok(insumoService.actualizar(id, dto));
    }

    @Operation(summary = "Obtener insumo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<InsumoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(insumoService.obtenerPorId(id));
    }

    @Operation(summary = "Listar todos los insumos")
    @GetMapping
    public ResponseEntity<List<InsumoResponseDTO>> listar() {
        return ResponseEntity.ok(insumoService.listar());
    }

    @Operation(summary = "Listar solo insumos activos")
    @GetMapping("/activos")
    public ResponseEntity<List<InsumoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(insumoService.listarActivos());
    }

    @Operation(summary = "Buscar insumos por nombre")
    @GetMapping("/buscar")
    public ResponseEntity<List<InsumoResponseDTO>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(insumoService.buscar(nombre));
    }

    @Operation(summary = "Listar insumos por unidad de medida")
    @GetMapping("/unidad-medida/{unidadMedidaId}")
    public ResponseEntity<List<InsumoResponseDTO>> listarPorUnidadMedida(@PathVariable Long unidadMedidaId) {
        return ResponseEntity.ok(insumoService.listarPorUnidadMedida(unidadMedidaId));
    }

    @Operation(summary = "Listar insumos con stock bajo (stock actual <= stock mínimo)")
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<InsumoResponseDTO>> listarStockBajo() {
        return ResponseEntity.ok(insumoService.listarStockBajo());
    }

    @Operation(summary = "Ajustar stock manualmente", description = "Entrada o salida de stock con motivo")
    @PostMapping("/{id}/ajustar-stock")
    public ResponseEntity<InsumoResponseDTO> ajustarStock(
            @PathVariable Long id,
            @RequestParam Double cantidad,
            @RequestParam String tipo,      // "ENTRADA" o "SALIDA"
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(insumoService.ajustarStock(id, cantidad, tipo, motivo));
    }

    @Operation(summary = "Eliminar insumo", description = "Desactiva un insumo (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        insumoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}