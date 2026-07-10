/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/material/infrastructure/in/api/controllers/MaterialController.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: MaterialController
 * CONTEXTO: Adaptador de entrada HTTP: expone endpoints REST del modulo y delega en contratos de aplicacion.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.material.application.usecases.MaterialUseCase;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialCreateDTO;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialUpdateDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.MATERIALES)
public class MaterialController {

    private final MaterialUseCase materialService;

    public MaterialController(MaterialUseCase materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    public ResponseEntity<MaterialResponseDTO> crear(@Valid @RequestBody MaterialCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.crear(dto));
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda) {
        if (page != null) {
            PageResponseDTO<MaterialResponseDTO> resultado = materialService.obtenerPaginado(
                    page,
                    sortBy,
                    direction,
                    activo,
                    busqueda);
            return ResponseEntity.ok(resultado);
        }

        return ResponseEntity.ok(materialService.obtenerTodos());
    }

    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        byte[] excel = materialService.generarReporteExcel(activo, busqueda, sortBy, direction);
        String filename = "materiales.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<MaterialResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(materialService.obtenerActivos());
    }

    @GetMapping("/codigo-sugerido")
    public ResponseEntity<Map<String, String>> sugerirCodigo(@RequestParam String nombre) {
        return ResponseEntity.ok(Map.of("codigo", materialService.sugerirCodigo(nombre)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MaterialUpdateDTO dto) {
        return ResponseEntity.ok(materialService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<MaterialResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<MaterialResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.desactivar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        materialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}





