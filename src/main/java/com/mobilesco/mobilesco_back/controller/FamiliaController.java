// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/FamiliaController.java
// ============================================
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
import com.mobilesco.mobilesco_back.dto.familia.FamiliaCreateDTO;
import com.mobilesco.mobilesco_back.dto.familia.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.dto.familia.FamiliaUpdateDTO;
import com.mobilesco.mobilesco_back.services.FamiliaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.FAMILIAS)
public class FamiliaController {

    private final FamiliaService familiaService;

    public FamiliaController(FamiliaService familiaService) {
        this.familiaService = familiaService;
    }

    // ========== CREATE ==========
    
    @PostMapping
    public ResponseEntity<FamiliaResponseDTO> crear(@Valid @RequestBody FamiliaCreateDTO dto) {
        FamiliaResponseDTO creado = familiaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ========== READ ==========
    
    @GetMapping
    public ResponseEntity<List<FamiliaResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(familiaService.obtenerTodos());
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<FamiliaResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(familiaService.obtenerActivos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FamiliaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(familiaService.obtenerPorId(id));
    }
    
    @GetMapping("/por-linea/{lineaId}")
    public ResponseEntity<List<FamiliaResponseDTO>> obtenerPorLinea(@PathVariable Long lineaId) {
        return ResponseEntity.ok(familiaService.obtenerPorLinea(lineaId));
    }
    
    @GetMapping("/por-linea/{lineaId}/activos")
    public ResponseEntity<List<FamiliaResponseDTO>> obtenerPorLineaYActivo(
            @PathVariable Long lineaId,
            @RequestParam Boolean activo) {
        return ResponseEntity.ok(familiaService.obtenerPorLineaYActivo(lineaId, activo));
    }

    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    public ResponseEntity<FamiliaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody FamiliaUpdateDTO dto) {
        return ResponseEntity.ok(familiaService.actualizar(id, dto));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        familiaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}