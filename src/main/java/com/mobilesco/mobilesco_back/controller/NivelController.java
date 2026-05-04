// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/NivelController.java
// ============================================
package com.mobilesco.mobilesco_back.controller;

import java.util.List;

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
import com.mobilesco.mobilesco_back.dto.nivel.NivelCreateDTO;
import com.mobilesco.mobilesco_back.dto.nivel.NivelResponseDTO;
import com.mobilesco.mobilesco_back.dto.nivel.NivelUpdateDTO;
import com.mobilesco.mobilesco_back.services.NivelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.NIVELES)
public class NivelController {

    private final NivelService nivelService;

    public NivelController(NivelService nivelService) {
        this.nivelService = nivelService;
    }

    // ========== CREATE ==========
    
    @PostMapping
    public ResponseEntity<NivelResponseDTO> crear(@Valid @RequestBody NivelCreateDTO dto) {
        NivelResponseDTO creado = nivelService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ========== READ ==========
    
    @GetMapping
    public ResponseEntity<List<NivelResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(nivelService.obtenerTodos());
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<NivelResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(nivelService.obtenerActivos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NivelResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(nivelService.obtenerPorId(id));
    }
    
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<NivelResponseDTO> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(nivelService.obtenerPorCodigo(codigo));
    }
    
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<NivelResponseDTO> obtenerPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(nivelService.obtenerPorNombre(nombre));
    }

    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        byte[] excel = nivelService.generarReporteExcel(activo, busqueda, sortBy, direction);
        String filename = "niveles.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    public ResponseEntity<NivelResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody NivelUpdateDTO dto) {
        return ResponseEntity.ok(nivelService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<NivelResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(nivelService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<NivelResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(nivelService.desactivar(id));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        nivelService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
