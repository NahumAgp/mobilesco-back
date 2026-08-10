package com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.application.usecases.RequisicionAlmacenService;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.EstadoRequisicionAlmacen;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.InsumoRequisicionDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionCreateDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionEstadoDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.REQUISICIONES_ALMACEN)
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_WAREHOUSE_REQUISITIONS')")
public class RequisicionAlmacenController {

    private final RequisicionAlmacenService requisicionService;

    @GetMapping
    public ResponseEntity<Page<RequisicionResponseDTO>> listar(
            @RequestParam(required = false) EstadoRequisicionAlmacen estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "fechaEnvio"));
        return ResponseEntity.ok(requisicionService.listar(
                estado, busqueda, pageable, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequisicionResponseDTO> obtener(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(requisicionService.obtener(id, authentication.getName()));
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<List<InsumoRequisicionDTO>> sugerencias() {
        return ResponseEntity.ok(requisicionService.sugerencias());
    }

    @GetMapping("/insumos")
    public ResponseEntity<List<InsumoRequisicionDTO>> buscarInsumos(
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(requisicionService.buscarInsumos(busqueda));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_WAREHOUSE_REQUISITIONS') and hasAuthority('ACTION_WAREHOUSE_REQUISITIONS_CREATE')")
    public ResponseEntity<RequisicionResponseDTO> crear(
            @Valid @RequestBody RequisicionCreateDTO dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requisicionService.crear(dto, authentication.getName()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<RequisicionResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody RequisicionEstadoDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(requisicionService.cambiarEstado(id, dto, authentication.getName()));
    }
}
