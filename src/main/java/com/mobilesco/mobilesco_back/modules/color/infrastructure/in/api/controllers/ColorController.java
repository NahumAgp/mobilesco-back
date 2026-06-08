/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/infrastructure/in/api/controllers/ColorController.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ColorController
 * CONTEXTO: Adaptador de entrada HTTP: expone endpoints REST del modulo y delega en contratos de aplicacion.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorCreateDTO;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorResponseDTO;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorUpdateDTO;
import com.mobilesco.mobilesco_back.modules.color.application.usecases.ColorUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.COLORES)
public class ColorController {

    private final ColorUseCase colorService;

    public ColorController(ColorUseCase colorService) {
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

    @GetMapping("/codigo-sugerido")
    public ResponseEntity<Map<String, String>> sugerirCodigo(@RequestParam String hex) {
        return ResponseEntity.ok(Map.of("codigo", colorService.sugerirCodigo(hex)));
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

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ColorResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(colorService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ColorResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(colorService.desactivar(id));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        colorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}





