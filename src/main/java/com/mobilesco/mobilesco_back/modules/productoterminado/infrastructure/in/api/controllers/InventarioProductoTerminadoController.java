package com.mobilesco.mobilesco_back.modules.productoterminado.infrastructure.in.api.controllers;

import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.productoterminado.application.usecases.InventarioProductoTerminadoService;
import com.mobilesco.mobilesco_back.modules.productoterminado.infrastructure.in.api.dtos.InventarioProductoTerminadoResponseDTO;
import lombok.RequiredArgsConstructor;

@RestController @RequestMapping(ApiPaths.INVENTARIO_PRODUCTOS_TERMINADOS) @RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_FINISHED_GOODS')")
public class InventarioProductoTerminadoController {
    private final InventarioProductoTerminadoService service;
    @GetMapping
    public PageResponseDTO<InventarioProductoTerminadoResponseDTO> listar(@RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.listar(busqueda, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
                Sort.by(Sort.Direction.ASC, "producto.sku")));
    }
}
