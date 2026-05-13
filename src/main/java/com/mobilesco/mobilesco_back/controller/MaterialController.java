package com.mobilesco.mobilesco_back.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.material.MaterialCreateDTO;
import com.mobilesco.mobilesco_back.dto.material.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.dto.material.MaterialUpdateDTO;
import com.mobilesco.mobilesco_back.services.MaterialService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.MATERIALES)
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    public ResponseEntity<MaterialResponseDTO> crear(@Valid @RequestBody MaterialCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.crear(dto));
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(materialService.obtenerTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<MaterialResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(materialService.obtenerActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MaterialUpdateDTO dto) {
        return ResponseEntity.ok(materialService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<MaterialResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<MaterialResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.desactivar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        materialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
