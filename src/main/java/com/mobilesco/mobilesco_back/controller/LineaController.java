// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/LineaController.java
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
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.linea.LineaCreateDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaResponseDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaUpdateDTO;
import com.mobilesco.mobilesco_back.services.LineaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.LINEAS) // /api/v1/lineas
public class LineaController {

    private final LineaService lineaService;

    public LineaController(LineaService lineaService) {
        this.lineaService = lineaService;
    }

    // ========== CREATE ==========
    
    @PostMapping
    public ResponseEntity<LineaResponseDTO> crear(@Valid @RequestBody LineaCreateDTO dto) {
        LineaResponseDTO creado = lineaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ========== READ ==========
    
    @GetMapping
    public ResponseEntity<List<LineaResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(lineaService.obtenerTodos());
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<LineaResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(lineaService.obtenerActivos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LineaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(lineaService.obtenerPorId(id));
    }

    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    public ResponseEntity<LineaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LineaUpdateDTO dto) {
        return ResponseEntity.ok(lineaService.actualizar(id, dto));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        lineaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}