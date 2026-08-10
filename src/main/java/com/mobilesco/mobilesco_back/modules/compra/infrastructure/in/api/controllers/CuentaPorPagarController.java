package com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.controllers;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private static final String ROLES_GESTION_COMPRAS = "hasAuthority('VIEW_ACCOUNTS_PAYABLE') and hasAuthority('ACTION_ACCOUNTS_PAYABLE_EDIT')";

    private final CuentaPorPagarService cuentaPorPagarService;

    @Operation(summary = "Listar cuentas por pagar")
    @GetMapping
    @PreAuthorize(PERMISO_VER_COMPRAS)
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(cuentaPorPagarService.listar(estado, busqueda, fechaInicio, fechaFin));
        }

        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, 100) : 10;
        PageRequest pageable = PageRequest.of(
                pageNumber,
                pageSize,
                TypeSafeSorts.descWithId(CuentaPorPagarModel.class, CuentaPorPagarModel::getFechaCuenta, CuentaPorPagarModel::getId));

        return ResponseEntity.ok(cuentaPorPagarService.listarPaginado(estado, busqueda, fechaInicio, fechaFin, pageable));
    }

    @Operation(summary = "Exportar reporte de cuentas por pagar a Excel")
    @GetMapping("/reporte/excel")
    @PreAuthorize(PERMISO_VER_COMPRAS)
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        byte[] excel = cuentaPorPagarService.generarReporteExcel(estado, busqueda, fechaInicio, fechaFin);
        String filename = "cuentas-por-pagar.xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
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
