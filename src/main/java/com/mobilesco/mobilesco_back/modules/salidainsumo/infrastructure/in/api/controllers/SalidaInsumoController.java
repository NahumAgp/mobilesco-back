package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.SalidaInsumoModel;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.SalidaInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.SalidaInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.application.usecases.SalidaInsumoService;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Salidas de Insumos", description = "Registro manual de salidas de insumos por orden de producción")
@RestController
@RequestMapping(ApiPaths.SALIDAS_INSUMOS)
@RequiredArgsConstructor
public class SalidaInsumoController {

    private static final String PERMISO_VER_INVENTARIO = "hasAuthority('VIEW_INVENTORY_OUTPUTS')";
    private static final String ROLES_GESTION_INVENTARIO = "hasAuthority('VIEW_INVENTORY_OUTPUTS') and hasAuthority('ACTION_INVENTORY_OUTPUTS_CREATE')";
    private static final String ROLES_ELIMINAR_SALIDA = "hasAuthority('VIEW_INVENTORY_OUTPUTS') and hasAuthority('ACTION_INVENTORY_OUTPUTS_DELETE')";

    private final SalidaInsumoService salidaInsumoService;

    @Operation(summary = "Crear salida de insumos")
    @PostMapping
    @PreAuthorize(ROLES_GESTION_INVENTARIO)
    public ResponseEntity<SalidaInsumoResponseDTO> crear(@Valid @RequestBody SalidaInsumoCreateDTO dto) {
        return new ResponseEntity<>(salidaInsumoService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Listar salidas de insumos")
    @GetMapping
    @PreAuthorize(PERMISO_VER_INVENTARIO)
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String responsable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        if (page != null) {
            int pageNumber = Math.max(page, 0);
            int pageSize = size == null ? 10 : Math.max(1, Math.min(size, 100));
            LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
            LocalDateTime fin = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : null;
            PageRequest pageable = PageRequest.of(
                    pageNumber,
                    pageSize,
                    TypeSafeSorts.descWithId(SalidaInsumoModel.class, SalidaInsumoModel::getFechaSalida, SalidaInsumoModel::getId));
            return ResponseEntity.ok(salidaInsumoService.listarPaginado(
                    busqueda,
                    area,
                    responsable,
                    inicio,
                    fin,
                    pageable));
        }
        return ResponseEntity.ok(salidaInsumoService.listar());
    }

    @Operation(summary = "Obtener salida de insumos por ID")
    @GetMapping("/{id}")
    @PreAuthorize(PERMISO_VER_INVENTARIO)
    public ResponseEntity<SalidaInsumoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(salidaInsumoService.obtenerPorId(id));
    }

    @Operation(summary = "Eliminar salida de insumos por error de captura")
    @DeleteMapping("/{id}")
    @PreAuthorize(ROLES_ELIMINAR_SALIDA)
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        salidaInsumoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
