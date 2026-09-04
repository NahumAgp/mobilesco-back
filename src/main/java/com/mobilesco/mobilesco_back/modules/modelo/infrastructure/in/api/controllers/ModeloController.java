/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/modelo/infrastructure/in/api/controllers/ModeloController.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ModeloController
 * CONTEXTO: Controlador REST del modulo Modelo.
 * NOTAS: Expone CRUD, filtros y gestion de imagenes de modelo.
 */
package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.controllers;

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
import org.springframework.web.multipart.MultipartFile;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.application.usecases.ModeloService;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCreateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloInsumoDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloMedidasDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloUpdateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.SincronizacionInsumosVariantesResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.SincronizacionMedidasVariantesResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.MODELOS)
public class ModeloController {

    private final ModeloService modeloService;

    public ModeloController(ModeloService modeloService) {
        this.modeloService = modeloService;
    }

    @PostMapping
    public ResponseEntity<ModeloResponseDTO> crear(@Valid @RequestBody ModeloCreateDTO dto) {
        ModeloResponseDTO creado = modeloService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long familiaId,
            @RequestParam(required = false) Long lineaId) {
        if (page != null) {
            PageResponseDTO<ModeloResponseDTO> resultado = modeloService.obtenerPaginado(
                    page,
                    sortBy,
                    direction,
                    activo,
                    busqueda,
                    familiaId,
                    lineaId);
            return ResponseEntity.ok(resultado);
        }

        return ResponseEntity.ok(modeloService.obtenerTodos());
    }

    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String familia,
            @RequestParam(required = false) Long familiaId,
            @RequestParam(required = false) Long lineaId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        byte[] excel = modeloService.generarReporteExcel(activo, busqueda, familia, familiaId, lineaId, sortBy, direction);
        String filename = "modelos.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ModeloResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(modeloService.obtenerActivos());
    }

    @GetMapping("/codigo-sugerido")
    public ResponseEntity<Map<String, String>> sugerirCodigo(
            @RequestParam String nombre,
            @RequestParam(required = false) Long familiaId,
            @RequestParam(required = false) Long subfamiliaId) {
        return ResponseEntity.ok(Map.of("codigo", modeloService.sugerirCodigo(nombre, familiaId, subfamiliaId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeloResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(modeloService.obtenerPorId(id));
    }

    @GetMapping("/por-familia/{familiaId}")
    public ResponseEntity<List<ModeloResponseDTO>> obtenerPorFamilia(@PathVariable Long familiaId) {
        return ResponseEntity.ok(modeloService.obtenerPorFamilia(familiaId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ModeloResponseDTO>> buscar(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long familiaId) {
        return ResponseEntity.ok(modeloService.buscarConFiltros(codigo, nombre, familiaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModeloResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ModeloUpdateDTO dto) {
        return ResponseEntity.ok(modeloService.actualizar(id, dto));
    }

    @PutMapping("/{id}/categorias/{nivelId}/insumos/sincronizar-variantes")
    public ResponseEntity<SincronizacionInsumosVariantesResponseDTO> sincronizarInsumosVariantes(
            @PathVariable Long id,
            @PathVariable Long nivelId,
            @RequestParam(required = false) Long materialId,
            @RequestBody List<@Valid ModeloInsumoDTO> insumos) {
        return ResponseEntity.ok(modeloService.sincronizarInsumosVariantes(id, nivelId, materialId, insumos));
    }

    @PutMapping("/{id}/categorias/{nivelId}/medidas/sincronizar-variantes")
    public ResponseEntity<SincronizacionMedidasVariantesResponseDTO> sincronizarMedidasVariantes(
            @PathVariable Long id,
            @PathVariable Long nivelId,
            @RequestParam(required = false) Long materialId,
            @Valid @RequestBody ModeloMedidasDTO medidas) {
        return ResponseEntity.ok(modeloService.sincronizarMedidasVariantes(id, nivelId, materialId, medidas));
    }

    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModeloResponseDTO> subirImagen(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(modeloService.actualizarImagen(id, archivo));
    }

    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<ModeloResponseDTO> eliminarImagen(@PathVariable Long id) {
        return ResponseEntity.ok(modeloService.eliminarImagen(id));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ModeloResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(modeloService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ModeloResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(modeloService.desactivar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        modeloService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
