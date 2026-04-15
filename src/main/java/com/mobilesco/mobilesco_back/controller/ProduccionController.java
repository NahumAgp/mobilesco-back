package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionCreateDTO;
import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionInsumoDTO;
import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionInsumoListaDTO;
import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionResponseDTO;
import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionTiempoDTO;
import com.mobilesco.mobilesco_back.services.ProduccionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Producción", description = "Gestión de órdenes de producción")
@RestController
@RequestMapping("/api/produccion")
@RequiredArgsConstructor
public class ProduccionController {

    private final ProduccionService produccionService;

    @Operation(summary = "Crear nueva orden de producción")
    @PostMapping
    public ResponseEntity<ProduccionResponseDTO> crear(@Valid @RequestBody ProduccionCreateDTO dto) {
        return new ResponseEntity<>(produccionService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Iniciar producción")
    @PostMapping("/{id}/iniciar")
    public ResponseEntity<ProduccionResponseDTO> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(produccionService.iniciarProduccion(id));
    }

    @Operation(summary = "Registrar consumo de insumo")
    @PostMapping("/{id}/insumos")
    public ResponseEntity<ProduccionResponseDTO> registrarConsumo(
            @PathVariable Long id,
            @Valid @RequestBody ProduccionInsumoDTO dto) {
        return ResponseEntity.ok(produccionService.registrarConsumoInsumo(id, dto));
    }

    @Operation(summary = "Registrar tiempo de operación")
    @PostMapping("/{id}/tiempos")
    public ResponseEntity<ProduccionResponseDTO> registrarTiempo(
            @PathVariable Long id,
            @Valid @RequestBody ProduccionTiempoDTO dto) {
        return ResponseEntity.ok(produccionService.registrarTiempoOperacion(id, dto));
    }

    @Operation(summary = "Finalizar producción")
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ProduccionResponseDTO> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(produccionService.finalizarProduccion(id));
    }

    @Operation(summary = "Obtener producción por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProduccionResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produccionService.obtenerPorId(id));
    }

    @Operation(summary = "Listar todas las producciones")
    @GetMapping
    public ResponseEntity<List<ProduccionResponseDTO>> listar() {
        return ResponseEntity.ok(produccionService.listar());
    }

    @Operation(summary = "Listar producciones por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ProduccionResponseDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(produccionService.listarPorEstado(estado));
    }

    @Operation(summary = "Registrar consumo masivo de insumos")
    @PostMapping("/{id}/insumos/masivo")
    public ResponseEntity<ProduccionResponseDTO> registrarConsumoMasivo(
            @PathVariable Long id,
            @Valid @RequestBody ProduccionInsumoListaDTO dto) {
        return ResponseEntity.ok(produccionService.registrarConsumoInsumosMasivo(id, dto.getInsumos()));
    }
}