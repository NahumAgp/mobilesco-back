package com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.in.api.controllers;

import java.time.LocalDate;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.application.usecases.OrdenProduccionService;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.*;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.in.api.dtos.*;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.in.api.dtos.OrdenProduccionAccionesDTO.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController @RequestMapping(ApiPaths.ORDENES_PRODUCCION) @RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_PRODUCTION_ORDERS')")
public class OrdenProduccionController {
    private final OrdenProduccionService service;
    @GetMapping public PageResponseDTO<OrdenProduccionResponseDTO> listar(@RequestParam(required=false) EstadoOrdenProduccion estado,
        @RequestParam(required=false) OrigenOrdenProduccion origen,@RequestParam(required=false) String busqueda,
        @RequestParam(required=false) LocalDate desde,@RequestParam(required=false) LocalDate hasta,
        @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size){
        return service.listar(estado,origen,busqueda,desde,hasta,PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),Sort.by(Sort.Direction.DESC,"fechaRegistro")));
    }
    @GetMapping("/{id}") public OrdenProduccionResponseDTO obtener(@PathVariable Long id){return service.obtener(id);}
    @PostMapping @PreAuthorize("hasAuthority('VIEW_PRODUCTION_ORDERS') and hasAuthority('ACTION_PRODUCTION_ORDERS_CREATE')")
    public ResponseEntity<OrdenProduccionResponseDTO> crear(@Valid @RequestBody OrdenProduccionRequestDTO dto,Authentication auth){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto,auth.getName()));}
    @PutMapping("/{id}") @PreAuthorize("hasAuthority('VIEW_PRODUCTION_ORDERS') and hasAuthority('ACTION_PRODUCTION_ORDERS_EDIT')")
    public OrdenProduccionResponseDTO actualizar(@PathVariable Long id,@Valid @RequestBody OrdenProduccionRequestDTO dto,Authentication auth){return service.actualizar(id,dto,auth.getName());}
    @PostMapping("/desde-cotizacion/{cotizacionId}") @PreAuthorize("hasAuthority('VIEW_PRODUCTION_ORDERS') and hasAuthority('ACTION_PRODUCTION_ORDERS_CREATE') and hasAuthority('VIEW_QUOTES')")
    public ResponseEntity<OrdenProduccionResponseDTO> convertir(@PathVariable Long cotizacionId,@RequestBody Conversion dto,Authentication auth){return ResponseEntity.status(HttpStatus.CREATED).body(service.convertirCotizacion(cotizacionId,dto,auth.getName()));}
    @PostMapping("/{id}/liberar") @PreAuthorize("hasAuthority('VIEW_PRODUCTION_ORDERS') and hasAuthority('ACTION_PRODUCTION_ORDERS_RELEASE')")
    public OrdenProduccionResponseDTO liberar(@PathVariable Long id,Authentication auth){return service.liberar(id,auth.getName());}
    @PostMapping("/{id}/surtidos") @PreAuthorize("hasAuthority('VIEW_PRODUCTION_ORDERS') and hasAuthority('ACTION_PRODUCTION_MATERIAL_ISSUE')")
    public OrdenProduccionResponseDTO surtir(@PathVariable Long id,@Valid @RequestBody Surtido dto,Authentication auth){return service.surtir(id,dto,auth.getName());}
    @PatchMapping("/{id}/operaciones/{operacionId}") @PreAuthorize("hasAuthority('VIEW_PRODUCTION_ORDERS') and hasAuthority('ACTION_PRODUCTION_PROGRESS')")
    public OrdenProduccionResponseDTO operacion(@PathVariable Long id,@PathVariable Long operacionId,@Valid @RequestBody CambioOperacion dto,Authentication auth){return service.cambiarOperacion(id,operacionId,dto,auth.getName());}
    @PostMapping("/{id}/partidas/{partidaId}/avances") @PreAuthorize("hasAuthority('VIEW_PRODUCTION_ORDERS') and hasAuthority('ACTION_PRODUCTION_PROGRESS')")
    public OrdenProduccionResponseDTO avance(@PathVariable Long id,@PathVariable Long partidaId,@Valid @RequestBody Avance dto,Authentication auth){return service.registrarAvance(id,partidaId,dto,auth.getName());}
    @PostMapping("/{id}/cancelar") @PreAuthorize("hasAuthority('VIEW_PRODUCTION_ORDERS') and hasAuthority('ACTION_PRODUCTION_ORDERS_CANCEL')")
    public OrdenProduccionResponseDTO cancelar(@PathVariable Long id,@Valid @RequestBody Cancelacion dto,Authentication auth){return service.cancelar(id,dto,auth.getName());}
}
