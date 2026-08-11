package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.controllers;

import static org.springframework.data.core.TypedPropertyPath.path;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.cotizacion.application.usecases.CotizacionService;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.CotizacionModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.COTIZACIONES)
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_QUOTES')")
public class CotizacionController {
    private final CotizacionService cotizacionService;

    @GetMapping
    public Page<CotizacionResponseDTO> listar(
            @RequestParam(required = false) EstadoCotizacion estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return cotizacionService.listar(estado, busqueda,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, path(CotizacionModel::getFechaRegistro))));
    }

    @GetMapping("/{id}")
    public CotizacionResponseDTO obtener(@PathVariable Long id) {
        return cotizacionService.obtener(id);
    }

    @GetMapping("/productos")
    public List<ProductoCotizableDTO> buscarProductos(
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "TODOS") String tipo) {
        return cotizacionService.buscarProductos(busqueda, tipo);
    }

    @PostMapping
    public ResponseEntity<CotizacionResponseDTO> crear(@Valid @RequestBody CotizacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacionService.crear(dto));
    }

    @PatchMapping("/{id}/estado")
    public CotizacionResponseDTO cambiarEstado(@PathVariable Long id, @RequestParam EstadoCotizacion estado) {
        return cotizacionService.cambiarEstado(id, estado);
    }
}
