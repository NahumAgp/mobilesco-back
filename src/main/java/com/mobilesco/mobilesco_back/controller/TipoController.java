// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/TipoController.java
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

import com.mobilesco.mobilesco_back.dto.tipo.TipoCreateDTO;
import com.mobilesco.mobilesco_back.dto.tipo.TipoResponseDTO;
import com.mobilesco.mobilesco_back.dto.tipo.TipoUpdateDTO;
import com.mobilesco.mobilesco_back.services.TipoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tipos")
public class TipoController {

    private final TipoService tipoService;

    public TipoController(TipoService tipoService) {
        this.tipoService = tipoService;
    }

    // ========== CREATE ==========
    
    @PostMapping
    public ResponseEntity<TipoResponseDTO> crear(@Valid @RequestBody TipoCreateDTO dto) {
        TipoResponseDTO creado = tipoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ========== READ ==========
    
    @GetMapping
    public ResponseEntity<List<TipoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(tipoService.obtenerTodos());
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<TipoResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(tipoService.obtenerActivos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TipoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoService.obtenerPorId(id));
    }
    
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<TipoResponseDTO> obtenerPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(tipoService.obtenerPorNombre(nombre));
    }

    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    public ResponseEntity<TipoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoUpdateDTO dto) {
        return ResponseEntity.ok(tipoService.actualizar(id, dto));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}