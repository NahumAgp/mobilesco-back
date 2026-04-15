package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.dto.DistribucionCosto.DistribucionCostoResponseDTO;
import com.mobilesco.mobilesco_back.dto.DistribucionCosto.DistribucionResumenDTO;
import com.mobilesco.mobilesco_back.services.DistribucionCostoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Distribución de Costos", description = "Asignación mensual de costos indirectos a productos")
@RestController
@RequestMapping("/api/distribucion-costos")
@RequiredArgsConstructor
public class DistribucionCostoController {

    private final DistribucionCostoService distribucionService;

    @Operation(summary = "Calcular y guardar distribución para un mes")
    @PostMapping("/calcular/{anio}/{mes}")
    public ResponseEntity<List<DistribucionCostoResponseDTO>> calcularDistribucion(
            @PathVariable Integer anio,
            @PathVariable Integer mes) {
        return ResponseEntity.ok(distribucionService.calcularDistribucionMensual(anio, mes));
    }

    @Operation(summary = "Obtener distribución de un período")
    @GetMapping("/{anio}/{mes}")
    public ResponseEntity<List<DistribucionCostoResponseDTO>> obtenerPorPeriodo(
            @PathVariable Integer anio,
            @PathVariable Integer mes) {
        return ResponseEntity.ok(distribucionService.obtenerDistribucionPorPeriodo(anio, mes));
    }

    @Operation(summary = "Obtener resumen de distribución por período")
    @GetMapping("/resumen/{anio}/{mes}")
    public ResponseEntity<DistribucionResumenDTO> obtenerResumen(
            @PathVariable Integer anio,
            @PathVariable Integer mes) {
        return ResponseEntity.ok(distribucionService.obtenerResumenPorPeriodo(anio, mes));
    }
}