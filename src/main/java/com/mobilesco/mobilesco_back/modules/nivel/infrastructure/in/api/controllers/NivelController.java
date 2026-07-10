// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/NivelController.java
// ============================================
package com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
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
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelCreateDTO;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelResponseDTO;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelUpdateDTO;
import com.mobilesco.mobilesco_back.modules.nivel.application.usecases.NivelService;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;

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
    public ResponseEntity<?> obtenerTodos(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        if (page != null) {
            int pageNumber = Math.max(page, 0);
            int pageSize = size == null ? 10 : Math.max(1, Math.min(size, 100));
            PageRequest pageable = PageRequest.of(
                    pageNumber,
                    pageSize,
                    TypeSafeSorts.ascWithId(NivelModel.class, NivelModel::getNombre, NivelModel::getId));
            return ResponseEntity.ok(nivelService.obtenerPaginado(activo, busqueda, pageable));
        }
        return ResponseEntity.ok(nivelService.obtenerTodos());
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<NivelResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(nivelService.obtenerActivos());
    }

    @GetMapping("/codigo-sugerido")
    public ResponseEntity<Map<String, String>> sugerirCodigo(
            @RequestParam String nombre,
            @RequestParam Long modeloId) {
        return ResponseEntity.ok(Map.of("codigo", nivelService.sugerirCodigo(nombre, modeloId)));
    }

    @GetMapping("/por-modelo/{modeloId}")
    public ResponseEntity<List<NivelResponseDTO>> obtenerPorModelo(
            @PathVariable Long modeloId,
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        return ResponseEntity.ok(nivelService.obtenerPorModelo(modeloId, soloActivos));
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
