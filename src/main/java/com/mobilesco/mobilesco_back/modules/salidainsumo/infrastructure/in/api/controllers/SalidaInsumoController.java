package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.SalidaInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.SalidaInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.application.usecases.SalidaInsumoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Salidas de Insumos", description = "Registro manual de salidas de insumos por orden de producción")
@RestController
@RequestMapping(ApiPaths.SALIDAS_INSUMOS)
@RequiredArgsConstructor
public class SalidaInsumoController {

    private static final String PERMISO_VER_INVENTARIO = "hasAuthority('VIEW_INVENTORY')";
    private static final String ROLES_GESTION_INVENTARIO = "hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA','JEFE_ALMACEN','ALMACEN')";

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
    public ResponseEntity<List<SalidaInsumoResponseDTO>> listar() {
        return ResponseEntity.ok(salidaInsumoService.listar());
    }

    @Operation(summary = "Obtener salida de insumos por ID")
    @GetMapping("/{id}")
    @PreAuthorize(PERMISO_VER_INVENTARIO)
    public ResponseEntity<SalidaInsumoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(salidaInsumoService.obtenerPorId(id));
    }
}
