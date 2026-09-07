package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.controllers;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.insumo.application.usecases.ConjuntoInsumoService;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.ConjuntoInsumoDTO;

@RestController
@RequestMapping(ApiPaths.INSUMOS + "/conjuntos")
@RequiredArgsConstructor
public class ConjuntoInsumoController {
    private final ConjuntoInsumoService service;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public List<ConjuntoInsumoDTO> listar() { return service.listar(); }

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') and hasAuthority('ACTION_INVENTORY_CREATE')")
    public ConjuntoInsumoDTO crear(@Valid @RequestBody ConjuntoInsumoDTO dto) { return service.guardar(null, dto); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') and hasAuthority('ACTION_INVENTORY_EDIT')")
    public ConjuntoInsumoDTO actualizar(@PathVariable Long id, @Valid @RequestBody ConjuntoInsumoDTO dto) {
        return service.guardar(id, dto);
    }
}
