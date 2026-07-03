package com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.mobilesco.mobilesco_back.modules.areatrabajo.application.usecases.AreaTrabajoService;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoCreateDTO;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoResponseDTO;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoUpdateDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.AREAS_TRABAJO)
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('VIEW_EMPLOYEES')")
public class AreaTrabajoController {

    private final AreaTrabajoService areaTrabajoService;

    public AreaTrabajoController(AreaTrabajoService areaTrabajoService) {
        this.areaTrabajoService = areaTrabajoService;
    }

    @GetMapping
    public ResponseEntity<List<AreaTrabajoResponseDTO>> listar(
            @RequestParam(required = false) Boolean activo
    ) {
        return ResponseEntity.ok(areaTrabajoService.listar(activo));
    }

    @GetMapping("/codigo-sugerido")
    public ResponseEntity<Map<String, String>> sugerirCodigo(@RequestParam String nombre) {
        return ResponseEntity.ok(Map.of("codigo", areaTrabajoService.sugerirCodigo(nombre)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaTrabajoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(areaTrabajoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<AreaTrabajoResponseDTO> crear(@Valid @RequestBody AreaTrabajoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(areaTrabajoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaTrabajoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AreaTrabajoUpdateDTO dto
    ) {
        return ResponseEntity.ok(areaTrabajoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<AreaTrabajoResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(areaTrabajoService.cambiarActivo(id, true));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<AreaTrabajoResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(areaTrabajoService.cambiarActivo(id, false));
    }
}
