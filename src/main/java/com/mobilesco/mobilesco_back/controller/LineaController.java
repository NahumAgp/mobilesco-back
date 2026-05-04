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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaCreateDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaResponseDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaUpdateDTO;
import com.mobilesco.mobilesco_back.services.LineaService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    public ResponseEntity<?> obtenerTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        if (page != null) {
            PageResponseDTO<LineaResponseDTO> resultado = lineaService.obtenerPaginado(page, sortBy, direction);
            return ResponseEntity.ok(resultado);
        }

        return ResponseEntity.ok(lineaService.obtenerTodos());
    }

    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        byte[] excel = lineaService.generarReporteExcel(activo, busqueda, sortBy, direction);
        String filename = "lineas_producto.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
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

    @PatchMapping("/{id}/activar")
    public ResponseEntity<LineaResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(lineaService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<LineaResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(lineaService.desactivar(id));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        lineaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
