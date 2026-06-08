/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/infrastructure/in/api/controllers/FamiliaController.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: FamiliaController
 * CONTEXTO: Adaptador de entrada HTTP: expone endpoints REST del modulo y delega en contratos de aplicacion.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaCreateDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaUpdateDTO;
import com.mobilesco.mobilesco_back.modules.familia.application.usecases.FamiliaUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.FAMILIAS)
public class FamiliaController {

    private final FamiliaUseCase familiaService;

    public FamiliaController(FamiliaUseCase familiaService) {
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
    public ResponseEntity<?> obtenerTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        if (page != null) {
            PageResponseDTO<FamiliaResponseDTO> resultado = familiaService.obtenerPaginado(page, sortBy, direction);
            return ResponseEntity.ok(resultado);
        }

        return ResponseEntity.ok(familiaService.obtenerTodos());
    }

    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long lineaId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        byte[] excel = familiaService.generarReporteExcel(activo, busqueda, lineaId, sortBy, direction);
        String filename = "familias.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<FamiliaResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(familiaService.obtenerActivos());
    }

    @GetMapping("/codigo-sugerido")
    public ResponseEntity<Map<String, String>> sugerirCodigo(@RequestParam String nombre) {
        return ResponseEntity.ok(Map.of("codigo", familiaService.sugerirCodigo(nombre)));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<FamiliaResponseDTO>> obtenerActivas() {
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

    @PatchMapping("/{id}/activar")
    public ResponseEntity<FamiliaResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(familiaService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<FamiliaResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(familiaService.desactivar(id));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        familiaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}





