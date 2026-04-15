package com.mobilesco.mobilesco_back.controller;

import com.mobilesco.mobilesco_back.dto.Compra.DetalleCompraCreateDTO;
import com.mobilesco.mobilesco_back.dto.Compra.DetalleCompraResponseDTO;
import com.mobilesco.mobilesco_back.dto.Compra.DetalleCompraUpdateDTO;
import com.mobilesco.mobilesco_back.services.DetalleCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Detalles de Compra", description = "Gestión de detalles de compras de insumos")
@RestController
@RequestMapping("/api/detalles-compra")
@RequiredArgsConstructor
public class DetalleCompraController {

    private final DetalleCompraService detalleCompraService;

    @Operation(summary = "Crear detalle para una compra")
    @PostMapping("/compra/{compraId}")
    public ResponseEntity<DetalleCompraResponseDTO> crear(
            @PathVariable Long compraId,
            @Valid @RequestBody DetalleCompraCreateDTO dto) {
        return new ResponseEntity<>(detalleCompraService.crear(compraId, dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar detalle")
    @PutMapping("/{id}")
    public ResponseEntity<DetalleCompraResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DetalleCompraUpdateDTO dto) {
        return ResponseEntity.ok(detalleCompraService.actualizar(id, dto));
    }

    @Operation(summary = "Obtener detalle por ID")
    @GetMapping("/{id}")
    public ResponseEntity<DetalleCompraResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(detalleCompraService.obtenerPorId(id));
    }

    @Operation(summary = "Listar detalles por compra")
    @GetMapping("/compra/{compraId}")
    public ResponseEntity<List<DetalleCompraResponseDTO>> listarPorCompra(@PathVariable Long compraId) {
        return ResponseEntity.ok(detalleCompraService.listarPorCompra(compraId));
    }

    @Operation(summary = "Listar compras de un insumo")
    @GetMapping("/insumo/{insumoId}")
    public ResponseEntity<List<DetalleCompraResponseDTO>> listarPorInsumo(@PathVariable Long insumoId) {
        return ResponseEntity.ok(detalleCompraService.listarPorInsumo(insumoId));
    }

    @Operation(summary = "Registrar recepción parcial")
    @PatchMapping("/{id}/recibir")
    public ResponseEntity<DetalleCompraResponseDTO> recibirParcial(
            @PathVariable Long id,
            @RequestParam Double cantidadRecibida) {
        return ResponseEntity.ok(detalleCompraService.recibirParcial(id, cantidadRecibida));
    }

    @Operation(summary = "Eliminar detalle")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        detalleCompraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}