// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/ColorController.java
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

import com.mobilesco.mobilesco_back.dto.color.ColorCreateDTO;
import com.mobilesco.mobilesco_back.dto.color.ColorResponseDTO;
import com.mobilesco.mobilesco_back.dto.color.ColorUpdateDTO;
import com.mobilesco.mobilesco_back.services.ColorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/colores")
public class ColorController {

    private final ColorService colorService;

    public ColorController(ColorService colorService) {
        this.colorService = colorService;
    }

    // ========== CREATE ==========
    
    @PostMapping
    public ResponseEntity<ColorResponseDTO> crear(@Valid @RequestBody ColorCreateDTO dto) {
        ColorResponseDTO creado = colorService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ========== READ ==========
    
    @GetMapping
    public ResponseEntity<List<ColorResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(colorService.obtenerTodos());
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<ColorResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(colorService.obtenerActivos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ColorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(colorService.obtenerPorId(id));
    }
    
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<ColorResponseDTO> obtenerPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(colorService.obtenerPorNombre(nombre));
    }

    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    public ResponseEntity<ColorResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ColorUpdateDTO dto) {
        return ResponseEntity.ok(colorService.actualizar(id, dto));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        colorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}