package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.costoindirecto.application.usecases.CostoIndirectoService;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CifConfiguracionDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CifEstadoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CifResumenDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CostoIndirectoCreateDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CostoIndirectoResponseDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CostoIndirectoUpdateDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "CIF", description = "Costos indirectos de fabricacion")
@RestController
@RequestMapping(ApiPaths.CIF)
@RequiredArgsConstructor
public class CifController {

    private static final String ROLES_GESTION_CIF =
            "hasAnyRole('ADMIN','SUPER_ADMIN','SUBDIRECCION_ADMINISTRATIVA')";

    private final CostoIndirectoService costoIndirectoService;

    @GetMapping("/conceptos")
    public ResponseEntity<List<CostoIndirectoResponseDTO>> listarConceptos() {
        return ResponseEntity.ok(costoIndirectoService.listar());
    }

    @GetMapping("/conceptos/{id}")
    public ResponseEntity<CostoIndirectoResponseDTO> obtenerConcepto(@PathVariable Long id) {
        return ResponseEntity.ok(costoIndirectoService.obtenerPorId(id));
    }

    @PostMapping("/conceptos")
    @PreAuthorize(ROLES_GESTION_CIF)
    public ResponseEntity<CostoIndirectoResponseDTO> crearConcepto(@Valid @RequestBody CostoIndirectoCreateDTO dto) {
        return new ResponseEntity<>(costoIndirectoService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("/conceptos/{id}")
    @PreAuthorize(ROLES_GESTION_CIF)
    public ResponseEntity<CostoIndirectoResponseDTO> actualizarConcepto(
            @PathVariable Long id,
            @Valid @RequestBody CostoIndirectoUpdateDTO dto) {
        return ResponseEntity.ok(costoIndirectoService.actualizar(id, dto));
    }

    @PatchMapping("/conceptos/{id}/estado")
    @PreAuthorize(ROLES_GESTION_CIF)
    public ResponseEntity<CostoIndirectoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CifEstadoUpdateDTO dto) {
        return ResponseEntity.ok(costoIndirectoService.cambiarEstado(id, dto.getActivo()));
    }

    @DeleteMapping("/conceptos/{id}")
    @PreAuthorize(ROLES_GESTION_CIF)
    public ResponseEntity<Void> eliminarConcepto(@PathVariable Long id) {
        costoIndirectoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/configuracion")
    public ResponseEntity<CifConfiguracionDTO> obtenerConfiguracion() {
        return ResponseEntity.ok(costoIndirectoService.obtenerConfiguracion());
    }

    @PutMapping("/configuracion")
    @PreAuthorize(ROLES_GESTION_CIF)
    public ResponseEntity<CifConfiguracionDTO> actualizarConfiguracion(@RequestBody CifConfiguracionDTO dto) {
        return ResponseEntity.ok(costoIndirectoService.actualizarConfiguracion(dto));
    }

    @GetMapping("/resumen")
    public ResponseEntity<CifResumenDTO> obtenerResumen() {
        return ResponseEntity.ok(costoIndirectoService.obtenerResumen());
    }
}
