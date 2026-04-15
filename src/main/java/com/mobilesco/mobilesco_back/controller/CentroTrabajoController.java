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
import com.mobilesco.mobilesco_back.dto.CentroTrabajo.CentroTrabajoCreateDTO;
import com.mobilesco.mobilesco_back.dto.CentroTrabajo.CentroTrabajoResponseDTO;
import com.mobilesco.mobilesco_back.dto.CentroTrabajo.CentroTrabajoUpdateDTO;
import com.mobilesco.mobilesco_back.services.CentroTrabajoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Centros de Trabajo", description = "Catálogo de centros de trabajo (máquinas y estaciones)")
@RestController
@RequestMapping(ApiPaths.CENTRO_TRABAJO)
@RequiredArgsConstructor
public class CentroTrabajoController {

    private final CentroTrabajoService centroTrabajoService;

    @Operation(summary = "Crear nuevo centro de trabajo")
    @PostMapping
    public ResponseEntity<CentroTrabajoResponseDTO> crear(@Valid @RequestBody CentroTrabajoCreateDTO dto) {
        return new ResponseEntity<>(centroTrabajoService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar centro de trabajo")
    @PutMapping("/{id}")
    public ResponseEntity<CentroTrabajoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CentroTrabajoUpdateDTO dto) {
        return ResponseEntity.ok(centroTrabajoService.actualizar(id, dto));
    }

    @Operation(summary = "Obtener centro de trabajo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CentroTrabajoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(centroTrabajoService.obtenerPorId(id));
    }

    @Operation(summary = "Obtener centro de trabajo por código")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<CentroTrabajoResponseDTO> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(centroTrabajoService.obtenerPorCodigo(codigo));
    }

    @Operation(summary = "Listar todos los centros de trabajo")
    @GetMapping
    public ResponseEntity<List<CentroTrabajoResponseDTO>> listar() {
        return ResponseEntity.ok(centroTrabajoService.listar());
    }

    @Operation(summary = "Listar solo centros activos")
    @GetMapping("/activos")
    public ResponseEntity<List<CentroTrabajoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(centroTrabajoService.listarActivos());
    }

    @Operation(summary = "Buscar centros por nombre")
    @GetMapping("/buscar")
    public ResponseEntity<List<CentroTrabajoResponseDTO>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(centroTrabajoService.buscar(nombre));
    }

    @Operation(summary = "Eliminar centro de trabajo (desactivar)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        centroTrabajoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}