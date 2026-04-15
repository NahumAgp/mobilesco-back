package com.mobilesco.mobilesco_back.controller;

import com.mobilesco.mobilesco_back.dto.Kardex.MovimientoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.services.KardexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Kardex", description = "Historial de movimientos de insumos")
@RestController
@RequestMapping("/api/kardex")
@RequiredArgsConstructor
public class KardexController {

    private final KardexService kardexService;

    @Operation(summary = "Obtener historial de un insumo")
    @GetMapping("/insumo/{insumoId}")
    public ResponseEntity<List<MovimientoInsumoResponseDTO>> getHistorialPorInsumo(
            @PathVariable Long insumoId) {
        return ResponseEntity.ok(kardexService.obtenerHistorialPorInsumo(insumoId));
    }

    @Operation(summary = "Obtener movimientos por período")
    @GetMapping("/periodo")
    public ResponseEntity<List<MovimientoInsumoResponseDTO>> getMovimientosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(kardexService.obtenerMovimientosPorPeriodo(fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener movimientos de una compra")
    @GetMapping("/compra/{compraId}")
    public ResponseEntity<List<MovimientoInsumoResponseDTO>> getMovimientosPorCompra(
            @PathVariable Long compraId) {
        return ResponseEntity.ok(kardexService.obtenerMovimientosPorCompra(compraId));
    }

    @Operation(summary = "Calcular costo promedio de un insumo")
    @GetMapping("/insumo/{insumoId}/costo-promedio")
    public ResponseEntity<Double> getCostoPromedio(@PathVariable Long insumoId) {
        return ResponseEntity.ok(kardexService.calcularCostoPromedio(insumoId));
    }

    @Operation(summary = "Calcular consumo en un período")
    @GetMapping("/insumo/{insumoId}/consumo")
    public ResponseEntity<Double> getConsumoEnPeriodo(
            @PathVariable Long insumoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(kardexService.calcularConsumoEnPeriodo(insumoId, fechaInicio, fechaFin));
    }
}