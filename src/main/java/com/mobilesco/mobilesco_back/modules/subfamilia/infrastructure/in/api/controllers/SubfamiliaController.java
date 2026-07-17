package com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.in.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.subfamilia.application.usecases.SubfamiliaService;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.in.api.dtos.SubfamiliaCreateDTO;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.in.api.dtos.SubfamiliaResponseDTO;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.in.api.dtos.SubfamiliaUpdateDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.SUBFAMILIAS)
public class SubfamiliaController {

    private final SubfamiliaService subfamiliaService;

    public SubfamiliaController(SubfamiliaService subfamiliaService) {
        this.subfamiliaService = subfamiliaService;
    }

    @PostMapping
    public ResponseEntity<SubfamiliaResponseDTO> crear(@Valid @RequestBody SubfamiliaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subfamiliaService.crear(dto));
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long familiaId) {
        if (page != null) {
            PageResponseDTO<SubfamiliaResponseDTO> resultado = subfamiliaService.obtenerPaginado(
                    page, sortBy, direction, activo, busqueda, familiaId);
            return ResponseEntity.ok(resultado);
        }
        return ResponseEntity.ok(subfamiliaService.obtenerTodos());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<SubfamiliaResponseDTO>> obtenerActivas() {
        return ResponseEntity.ok(subfamiliaService.obtenerActivas());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<SubfamiliaResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(subfamiliaService.obtenerActivas());
    }

    @GetMapping("/codigo-sugerido")
    public ResponseEntity<Map<String, String>> sugerirCodigo(
            @RequestParam String nombre,
            @RequestParam(required = false) Long familiaId) {
        return ResponseEntity.ok(Map.of("codigo", subfamiliaService.sugerirCodigo(nombre, familiaId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubfamiliaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(subfamiliaService.obtenerPorId(id));
    }

    @GetMapping("/por-familia/{familiaId}")
    public ResponseEntity<List<SubfamiliaResponseDTO>> obtenerPorFamilia(@PathVariable Long familiaId) {
        return ResponseEntity.ok(subfamiliaService.obtenerPorFamilia(familiaId));
    }

    @GetMapping("/por-familia/{familiaId}/activos")
    public ResponseEntity<List<SubfamiliaResponseDTO>> obtenerPorFamiliaYActivo(
            @PathVariable Long familiaId,
            @RequestParam Boolean activo) {
        return ResponseEntity.ok(subfamiliaService.obtenerPorFamiliaYActivo(familiaId, activo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubfamiliaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SubfamiliaUpdateDTO dto) {
        return ResponseEntity.ok(subfamiliaService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<SubfamiliaResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(subfamiliaService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<SubfamiliaResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(subfamiliaService.desactivar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        subfamiliaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
