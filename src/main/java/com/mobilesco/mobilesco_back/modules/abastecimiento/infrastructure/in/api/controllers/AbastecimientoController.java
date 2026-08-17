package com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.abastecimiento.application.usecases.AbastecimientoService;
import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.ComprasBorradorResponseDTO;
import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.CrearComprasBorradorRequestDTO;
import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.SugerenciaAbastecimientoDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Abastecimiento", description = "Sugerencias de compra y generación de borradores")
@RestController
@RequestMapping(ApiPaths.ABASTECIMIENTO)
@RequiredArgsConstructor
public class AbastecimientoController {

    private final AbastecimientoService abastecimientoService;

    @Operation(summary = "Obtener sugerencias de abastecimiento")
    @GetMapping("/sugerencias")
    @PreAuthorize("hasAuthority('VIEW_PURCHASES')")
    public ResponseEntity<List<SugerenciaAbastecimientoDTO>> obtenerSugerencias() {
        return ResponseEntity.ok(abastecimientoService.obtenerSugerencias());
    }

    @Operation(summary = "Crear compras en borrador agrupadas por proveedor")
    @PostMapping("/compras-borrador")
    @PreAuthorize("hasAuthority('VIEW_PURCHASES') and hasAuthority('ACTION_PURCHASES_CREATE')")
    public ResponseEntity<ComprasBorradorResponseDTO> crearComprasBorrador(
            @Valid @RequestBody CrearComprasBorradorRequestDTO request) {
        return new ResponseEntity<>(abastecimientoService.crearComprasBorrador(request), HttpStatus.CREATED);
    }
}
