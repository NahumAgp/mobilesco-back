package com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.compra.application.usecases.CuentaPorPagarService;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.CuentaPorPagarModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CuentaPorPagarResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.PagoCuentaPorPagarCreateDTO;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Cuentas por pagar", description = "Control de adeudos a proveedores y pagos")
@RestController
@RequestMapping(ApiPaths.CUENTAS_POR_PAGAR)
@RequiredArgsConstructor
public class CuentaPorPagarController {

    private static final String PERMISO_VER_COMPRAS = "hasAuthority('VIEW_PURCHASES')";
    private static final String ROLES_GESTION_COMPRAS = "hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA','JEFE_ALMACEN')";

    private final CuentaPorPagarService cuentaPorPagarService;

    @Operation(summary = "Listar cuentas por pagar")
    @GetMapping
    @PreAuthorize(PERMISO_VER_COMPRAS)
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(cuentaPorPagarService.listar(estado));
        }

        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, 100) : 10;
        PageRequest pageable = PageRequest.of(
                pageNumber,
                pageSize,
                TypeSafeSorts.descWithId(CuentaPorPagarModel.class, CuentaPorPagarModel::getFechaCuenta, CuentaPorPagarModel::getId));

        return ResponseEntity.ok(cuentaPorPagarService.listarPaginado(estado, busqueda, pageable));
    }

    @Operation(summary = "Obtener detalle de cuenta por pagar")
    @GetMapping("/{id}")
    @PreAuthorize(PERMISO_VER_COMPRAS)
    public ResponseEntity<CuentaPorPagarResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaPorPagarService.obtenerPorId(id));
    }

    @Operation(summary = "Registrar pago parcial o total")
    @PostMapping("/{id}/pagos")
    @PreAuthorize(ROLES_GESTION_COMPRAS)
    public ResponseEntity<CuentaPorPagarResponseDTO> registrarPago(
            @PathVariable Long id,
            @Valid @RequestBody PagoCuentaPorPagarCreateDTO dto) {
        return ResponseEntity.ok(cuentaPorPagarService.registrarPago(id, dto));
    }
}
