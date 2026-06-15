package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.mobilesco.mobilesco_back.modules.insumo.domain.enums.TipoInsumo;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoCostoCotizacionUpdateDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoCostoResponseDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoEstadoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.insumo.application.usecases.InsumoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Insumos", description = "CRUD y gestión de insumos (materia prima)")
@RestController
@RequestMapping(ApiPaths.INSUMOS)
@RequiredArgsConstructor
public class InsumoController {

    private static final String ROLES_GESTION_INSUMOS =
            "hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL','JEFE_ALMACEN','ALMACEN','SUBDIRECCION_ADMINISTRATIVA')";

    private final InsumoService insumoService;

    @Operation(summary = "Crear insumo", description = "Crea un nuevo insumo")
    @PostMapping
    @PreAuthorize(ROLES_GESTION_INSUMOS)
    public ResponseEntity<InsumoResponseDTO> crear(@Valid @RequestBody InsumoCreateDTO dto) {
        return new ResponseEntity<>(insumoService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar insumo", description = "Actualiza un insumo existente")
    @PutMapping("/{id}")
    @PreAuthorize(ROLES_GESTION_INSUMOS)
    public ResponseEntity<InsumoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody InsumoUpdateDTO dto) {
        return ResponseEntity.ok(insumoService.actualizar(id, dto));
    }

    @Operation(summary = "Obtener insumo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<InsumoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(insumoService.obtenerPorId(id));
    }

    @Operation(summary = "Listar todos los insumos")
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        if (page != null) {
            return ResponseEntity.ok(insumoService.listarPaginado(page, size, sortBy, direction));
        }

        return ResponseEntity.ok(insumoService.listar());
    }

    @Operation(summary = "Listar costos de insumos")
    @GetMapping("/costos")
    public ResponseEntity<PageResponseDTO<InsumoCostoResponseDTO>> listarCostos(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        return ResponseEntity.ok(insumoService.listarCostosPaginado(page, size, sortBy, direction));
    }

    @Operation(summary = "Actualizar costo de cotizacion")
    @PatchMapping("/{id}/costo-cotizacion")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','SUBDIRECCION_ADMINISTRATIVA')")
    public ResponseEntity<InsumoCostoResponseDTO> actualizarCostoCotizacion(
            @PathVariable Long id,
            @Valid @RequestBody InsumoCostoCotizacionUpdateDTO dto) {
        return ResponseEntity.ok(insumoService.actualizarCostoCotizacion(id, dto.getCostoCotizacion()));
    }

    @Operation(summary = "Actualizar estado del insumo")
    @PatchMapping("/{id}/estado")
    @PreAuthorize(ROLES_GESTION_INSUMOS)
    public ResponseEntity<InsumoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody InsumoEstadoUpdateDTO dto) {
        return ResponseEntity.ok(insumoService.actualizarEstado(id, dto.getActivo()));
    }

    @Operation(summary = "Exportar insumos a Excel")
    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Boolean stockBajo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        byte[] excel = insumoService.generarReporteExcel(activo, stockBajo, busqueda, sortBy, direction);
        String filename = "insumos.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @Operation(summary = "Listar solo insumos activos")
    @GetMapping("/activos")
    public ResponseEntity<List<InsumoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(insumoService.listarActivos());
    }

    @Operation(summary = "Obtener todos los tipos de insumo")
    @GetMapping("/tipos-insumo")
    public ResponseEntity<TipoInsumo[]> getTiposInsumo() {
        return ResponseEntity.ok(insumoService.getTodosLosTipos());
    }

    @Operation(summary = "Buscar insumos por nombre")
    @GetMapping("/buscar")
    public ResponseEntity<List<InsumoResponseDTO>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(insumoService.buscar(nombre));
    }

    @Operation(summary = "Listar insumos por unidad de medida")
    @GetMapping("/unidad-medida/{unidadMedidaId}")
    public ResponseEntity<List<InsumoResponseDTO>> listarPorUnidadMedida(@PathVariable Long unidadMedidaId) {
        return ResponseEntity.ok(insumoService.listarPorUnidadMedida(unidadMedidaId));
    }

    @Operation(summary = "Listar insumos con stock bajo (stock actual <= stock mínimo)")
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<InsumoResponseDTO>> listarStockBajo() {
        return ResponseEntity.ok(insumoService.listarStockBajo());
    }

    @Operation(summary = "Ajustar stock manualmente", description = "Entrada o salida de stock con motivo")
    @PostMapping("/{id}/ajustar-stock")
    @PreAuthorize(ROLES_GESTION_INSUMOS)
    public ResponseEntity<InsumoResponseDTO> ajustarStock(
            @PathVariable Long id,
            @RequestParam Double cantidad,
            @RequestParam String tipo,      // "ENTRADA" o "SALIDA"
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(insumoService.ajustarStock(id, cantidad, tipo, motivo));
    }

    @Operation(summary = "Eliminar insumo", description = "Elimina definitivamente un insumo sin asociaciones")
    @DeleteMapping("/{id}")
    @PreAuthorize(ROLES_GESTION_INSUMOS)
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        insumoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
