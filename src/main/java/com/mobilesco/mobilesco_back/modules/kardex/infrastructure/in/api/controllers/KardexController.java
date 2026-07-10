package com.mobilesco.mobilesco_back.modules.kardex.infrastructure.in.api.controllers;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.kardex.domain.models.MovimientoInsumoModel;
import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.in.api.dtos.MovimientoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Kardex", description = "Historial de movimientos de insumos")
@RestController
@RequestMapping(ApiPaths.KARDEX)
@RequiredArgsConstructor
public class KardexController {

    private static final String PERMISO_VER_KARDEX = "hasAuthority('VIEW_KARDEX')";

    private final KardexService kardexService;

    @Operation(summary = "Obtener historial de un insumo")
    @GetMapping("/insumo/{insumoId}")
    @PreAuthorize(PERMISO_VER_KARDEX)
    public ResponseEntity<?> getHistorialPorInsumo(
            @PathVariable Long insumoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (page != null) {
            PageRequest pageable = PageRequest.of(
                    Math.max(page, 0),
                    Math.max(size == null ? 10 : size, 1),
                    TypeSafeSorts.desc(MovimientoInsumoModel.class, MovimientoInsumoModel::getFecha)
                            .and(TypeSafeSorts.descById(MovimientoInsumoModel.class, MovimientoInsumoModel::getId))
            );
            return ResponseEntity.ok(kardexService.obtenerHistorialPorInsumoPaginado(insumoId, fechaInicio, fechaFin, pageable));
        }
        return ResponseEntity.ok(kardexService.obtenerHistorialPorInsumo(insumoId));
    }

    @Operation(summary = "Obtener movimientos por período")
    @GetMapping("/periodo")
    @PreAuthorize(PERMISO_VER_KARDEX)
    public ResponseEntity<?> getMovimientosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (page != null) {
            PageRequest pageable = PageRequest.of(
                    Math.max(page, 0),
                    Math.max(size == null ? 10 : size, 1),
                    TypeSafeSorts.desc(MovimientoInsumoModel.class, MovimientoInsumoModel::getFecha)
                            .and(TypeSafeSorts.descById(MovimientoInsumoModel.class, MovimientoInsumoModel::getId))
            );
            return ResponseEntity.ok(kardexService.obtenerMovimientosPorPeriodoPaginado(fechaInicio, fechaFin, pageable));
        }
        return ResponseEntity.ok(kardexService.obtenerMovimientosPorPeriodo(fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener movimientos de una compra")
    @GetMapping("/compra/{compraId}")
    @PreAuthorize(PERMISO_VER_KARDEX)
    public ResponseEntity<List<MovimientoInsumoResponseDTO>> getMovimientosPorCompra(
            @PathVariable Long compraId) {
        return ResponseEntity.ok(kardexService.obtenerMovimientosPorCompra(compraId));
    }

    @Operation(summary = "Calcular costo promedio de un insumo")
    @GetMapping("/insumo/{insumoId}/costo-promedio")
    @PreAuthorize(PERMISO_VER_KARDEX)
    public ResponseEntity<Double> getCostoPromedio(@PathVariable Long insumoId) {
        return ResponseEntity.ok(kardexService.calcularCostoPromedio(insumoId));
    }

    @Operation(summary = "Calcular consumo en un período")
    @GetMapping("/insumo/{insumoId}/consumo")
    @PreAuthorize(PERMISO_VER_KARDEX)
    public ResponseEntity<Double> getConsumoEnPeriodo(
            @PathVariable Long insumoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(kardexService.calcularConsumoEnPeriodo(insumoId, fechaInicio, fechaFin));
    }
}
