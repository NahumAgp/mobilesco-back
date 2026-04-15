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

import com.mobilesco.mobilesco_back.dto.CostoIndirecto.CostoIndirectoCreateDTO;
import com.mobilesco.mobilesco_back.dto.CostoIndirecto.CostoIndirectoResponseDTO;
import com.mobilesco.mobilesco_back.dto.CostoIndirecto.CostoIndirectoUpdateDTO;
import com.mobilesco.mobilesco_back.enums.TipoCostoIndirecto;
import com.mobilesco.mobilesco_back.services.CostoIndirectoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Costos Indirectos (CIF)", description = "Catálogo de costos indirectos de fabricación")
@RestController
@RequestMapping("/api/costos-indirectos")
@RequiredArgsConstructor
public class CostoIndirectoController {

    private final CostoIndirectoService costoIndirectoService;

    @Operation(summary = "Crear nuevo costo indirecto")
    @PostMapping
    public ResponseEntity<CostoIndirectoResponseDTO> crear(@Valid @RequestBody CostoIndirectoCreateDTO dto) {
        return new ResponseEntity<>(costoIndirectoService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar costo indirecto")
    @PutMapping("/{id}")
    public ResponseEntity<CostoIndirectoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CostoIndirectoUpdateDTO dto) {
        return ResponseEntity.ok(costoIndirectoService.actualizar(id, dto));
    }

    @Operation(summary = "Obtener costo indirecto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CostoIndirectoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(costoIndirectoService.obtenerPorId(id));
    }

    @Operation(summary = "Obtener costo indirecto por código")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<CostoIndirectoResponseDTO> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(costoIndirectoService.obtenerPorCodigo(codigo));
    }

    @Operation(summary = "Listar todos los costos indirectos")
    @GetMapping
    public ResponseEntity<List<CostoIndirectoResponseDTO>> listar() {
        return ResponseEntity.ok(costoIndirectoService.listar());
    }

    @Operation(summary = "Listar solo costos indirectos activos")
    @GetMapping("/activos")
    public ResponseEntity<List<CostoIndirectoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(costoIndirectoService.listarActivos());
    }

    @Operation(summary = "Listar por tipo (FIJO/VARIABLE)")
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<CostoIndirectoResponseDTO>> listarPorTipo(@PathVariable TipoCostoIndirecto tipo) {
        return ResponseEntity.ok(costoIndirectoService.listarPorTipo(tipo));
    }

    @Operation(summary = "Buscar costos indirectos por nombre")
    @GetMapping("/buscar")
    public ResponseEntity<List<CostoIndirectoResponseDTO>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(costoIndirectoService.buscar(nombre));
    }

    @Operation(summary = "Eliminar costo indirecto (desactivar)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        costoIndirectoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}