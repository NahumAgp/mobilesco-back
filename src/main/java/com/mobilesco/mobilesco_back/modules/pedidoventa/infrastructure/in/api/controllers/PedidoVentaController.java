package com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.in.api.controllers;

import java.time.LocalDate;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.pedidoventa.application.usecases.PedidoVentaService;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.*;
import com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.in.api.dtos.PedidoVentaResponseDTO;
import lombok.*;
import lombok.RequiredArgsConstructor;

@RestController @RequestMapping(ApiPaths.PEDIDOS_VENTA) @RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_SALES_ORDERS')")
public class PedidoVentaController {
    private final PedidoVentaService service;

    @GetMapping
    public PageResponseDTO<PedidoVentaResponseDTO> listar(@RequestParam(required = false) EstadoPedidoVenta estado,
            @RequestParam(required = false) String busqueda, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listar(estado, busqueda, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "fechaRegistro")));
    }

    @GetMapping("/{id}") public PedidoVentaResponseDTO obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping("/desde-cotizacion/{cotizacionId}")
    @PreAuthorize("hasAuthority('VIEW_SALES_ORDERS') and hasAuthority('ACTION_SALES_ORDERS_CREATE') and hasAuthority('VIEW_QUOTES')")
    public ResponseEntity<PedidoVentaResponseDTO> convertir(@PathVariable Long cotizacionId,
            @RequestBody(required = false) ConversionRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.convertirCotizacion(cotizacionId,
                request == null ? null : request.getFechaCompromiso(), auth.getName()));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAuthority('VIEW_SALES_ORDERS') and hasAuthority('ACTION_SALES_ORDERS_EDIT')")
    public PedidoVentaResponseDTO confirmar(@PathVariable Long id, Authentication auth) { return service.confirmar(id, auth.getName()); }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('VIEW_SALES_ORDERS') and hasAuthority('ACTION_SALES_ORDERS_EDIT')")
    public PedidoVentaResponseDTO cancelar(@PathVariable Long id, Authentication auth) { return service.cancelar(id, auth.getName()); }

    @Data @NoArgsConstructor
    public static class ConversionRequest { private LocalDate fechaCompromiso; }
}
