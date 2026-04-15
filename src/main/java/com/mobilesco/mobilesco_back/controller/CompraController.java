package com.mobilesco.mobilesco_back.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.mobilesco.mobilesco_back.dto.Compra.CompraCreateDTO;
import com.mobilesco.mobilesco_back.dto.Compra.CompraResponseDTO;
import com.mobilesco.mobilesco_back.dto.Compra.CompraUpdateDTO;
import com.mobilesco.mobilesco_back.services.CompraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Compras", description = "Gestión de compras de insumos")
@RestController
@RequestMapping(ApiPaths.COMPRAS)
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @Operation(summary = "Crear nueva compra")
    @PostMapping
    public ResponseEntity<CompraResponseDTO> crear(@Valid @RequestBody CompraCreateDTO dto) {
        return new ResponseEntity<>(compraService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar compra")
    @PutMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CompraUpdateDTO dto) {
        return ResponseEntity.ok(compraService.actualizar(id, dto));
    }

    @Operation(summary = "Recibir compra (actualiza stock)")
    @PostMapping("/{id}/recibir")
    public ResponseEntity<CompraResponseDTO> recibirCompra(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.recibirCompra(id));
    }

    @Operation(summary = "Cancelar compra")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<CompraResponseDTO> cancelarCompra(
            @PathVariable Long id,
            @RequestParam String motivo) {
        return ResponseEntity.ok(compraService.cancelarCompra(id, motivo));
    }

    @Operation(summary = "Obtener compra por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtenerPorId(id));
    }

    @Operation(summary = "Listar todas las compras")
    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> listar() {
        return ResponseEntity.ok(compraService.listar());
    }

    @Operation(summary = "Listar compras por proveedor")
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<CompraResponseDTO>> listarPorProveedor(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(compraService.listarPorProveedor(proveedorId));
    }

    @Operation(summary = "Listar compras por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CompraResponseDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(compraService.listarPorEstado(estado));
    }

    @Operation(summary = "Listar compras por rango de fechas")
    @GetMapping("/rango-fechas")
    public ResponseEntity<List<CompraResponseDTO>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(compraService.listarPorRangoFechas(fechaInicio, fechaFin));
    }

    @Operation(summary = "Buscar compras por folio")
    @GetMapping("/buscar")
    public ResponseEntity<List<CompraResponseDTO>> buscarPorFolio(@RequestParam String folio) {
        return ResponseEntity.ok(compraService.buscarPorFolio(folio));
    }

    @Operation(summary = "Eliminar compra (desactivar)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        compraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}