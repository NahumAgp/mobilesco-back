// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/EstiloController.java
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

import com.mobilesco.mobilesco_back.dto.estilo.EstiloCreateDTO;
import com.mobilesco.mobilesco_back.dto.estilo.EstiloResponseDTO;
import com.mobilesco.mobilesco_back.dto.estilo.EstiloUpdateDTO;
import com.mobilesco.mobilesco_back.services.EstiloService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estilos")
public class EstiloController {

    private final EstiloService estiloService;

    public EstiloController(EstiloService estiloService) {
        this.estiloService = estiloService;
    }

    // ========== CREATE ==========
    
    @PostMapping
    public ResponseEntity<EstiloResponseDTO> crear(@Valid @RequestBody EstiloCreateDTO dto) {
        EstiloResponseDTO creado = estiloService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ========== READ ==========
    
    @GetMapping
    public ResponseEntity<List<EstiloResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(estiloService.obtenerTodos());
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<EstiloResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(estiloService.obtenerActivos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EstiloResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estiloService.obtenerPorId(id));
    }
    
    @GetMapping("/por-familia/{familiaId}")
    public ResponseEntity<List<EstiloResponseDTO>> obtenerPorFamilia(@PathVariable Long familiaId) {
        return ResponseEntity.ok(estiloService.obtenerPorFamilia(familiaId));
    }
    
    @GetMapping("/por-familia/{familiaId}/activos")
    public ResponseEntity<List<EstiloResponseDTO>> obtenerPorFamiliaYActivo(
            @PathVariable Long familiaId,
            @RequestParam Boolean activo) {
        return ResponseEntity.ok(estiloService.obtenerPorFamiliaYActivo(familiaId, activo));
    }

    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    public ResponseEntity<EstiloResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstiloUpdateDTO dto) {
        return ResponseEntity.ok(estiloService.actualizar(id, dto));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estiloService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}