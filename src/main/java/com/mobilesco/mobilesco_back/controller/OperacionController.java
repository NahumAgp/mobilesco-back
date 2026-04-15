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
import com.mobilesco.mobilesco_back.dto.Operacion.OperacionCreateDTO;
import com.mobilesco.mobilesco_back.dto.Operacion.OperacionResponseDTO;
import com.mobilesco.mobilesco_back.dto.Operacion.OperacionUpdateDTO;
import com.mobilesco.mobilesco_back.services.OperacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Operaciones", description = "Catálogo de operaciones de fabricación (Mano de Obra Directa)")
@RestController
@RequestMapping(ApiPaths.OPERACION)
@RequiredArgsConstructor
public class OperacionController {

    private final OperacionService operacionService;

    @Operation(summary = "Crear nueva operación")
    @PostMapping
    public ResponseEntity<OperacionResponseDTO> crear(@Valid @RequestBody OperacionCreateDTO dto) {
        return new ResponseEntity<>(operacionService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar operación")
    @PutMapping("/{id}")
    public ResponseEntity<OperacionResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody OperacionUpdateDTO dto) {
        return ResponseEntity.ok(operacionService.actualizar(id, dto));
    }

    @Operation(summary = "Obtener operación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<OperacionResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(operacionService.obtenerPorId(id));
    }

    @Operation(summary = "Obtener operación por código")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<OperacionResponseDTO> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(operacionService.obtenerPorCodigo(codigo));
    }

    @Operation(summary = "Listar todas las operaciones")
    @GetMapping
    public ResponseEntity<List<OperacionResponseDTO>> listar() {
        return ResponseEntity.ok(operacionService.listar());
    }

    @Operation(summary = "Listar solo operaciones activas")
    @GetMapping("/activos")
    public ResponseEntity<List<OperacionResponseDTO>> listarActivos() {
        return ResponseEntity.ok(operacionService.listarActivos());
    }

    @Operation(summary = "Buscar operaciones por nombre")
    @GetMapping("/buscar")
    public ResponseEntity<List<OperacionResponseDTO>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(operacionService.buscar(nombre));
    }

    @Operation(summary = "Eliminar operación (desactivar)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        operacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}