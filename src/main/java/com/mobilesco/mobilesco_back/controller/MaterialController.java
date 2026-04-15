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
import com.mobilesco.mobilesco_back.dto.Material.MaterialCreateDTO;
import com.mobilesco.mobilesco_back.dto.Material.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.dto.Material.MaterialUpdateDTO;
import com.mobilesco.mobilesco_back.services.MaterialService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.MATERIALES)
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping
    public ResponseEntity<MaterialResponseDTO> crear(@Valid @RequestBody MaterialCreateDTO dto) {
        return new ResponseEntity<>(materialService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MaterialUpdateDTO dto) {
        return ResponseEntity.ok(materialService.actualizar(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponseDTO>> listar() {
        return ResponseEntity.ok(materialService.listar());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<MaterialResponseDTO>> listarActivos() {
        return ResponseEntity.ok(materialService.listarActivos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<MaterialResponseDTO>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(materialService.buscar(nombre));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        materialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}