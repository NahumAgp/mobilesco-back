package com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.cliente.application.usecases.ClienteService;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClasificacionCliente;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos.CatalogoClienteDTO;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos.ClienteRequestDTO;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos.ClienteResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Clientes", description = "Catálogo comercial de clientes")
@RestController
@RequestMapping(ApiPaths.CLIENTES)
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_CUSTOMERS')")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar clientes con paginación y filtros")
    public ResponseEntity<Page<ClienteResponseDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) ClasificacionCliente clasificacion,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort.Direction sentido = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC);
        Sort sort = crearOrden(sortBy, sentido);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);
        return ResponseEntity.ok(clienteService.listar(activo, clasificacion, busqueda, pageable));
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar clientes activos para selectores comerciales")
    public ResponseEntity<List<ClienteResponseDTO>> listarActivos() {
        return ResponseEntity.ok(clienteService.listarActivos());
    }

    @GetMapping("/clasificaciones")
    public ResponseEntity<List<CatalogoClienteDTO>> listarClasificaciones() {
        return ResponseEntity.ok(clienteService.listarClasificaciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estatus")
    public ResponseEntity<ClienteResponseDTO> cambiarEstatus(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(clienteService.cambiarEstatus(id, activo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_CUSTOMERS') and hasAuthority('ACTION_CUSTOMERS_DELETE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private Sort crearOrden(String sortBy, Sort.Direction direction) {
        boolean desc = direction == Sort.Direction.DESC;
        return switch (sortBy == null ? "nombre" : sortBy.toLowerCase()) {
            case "codigo" -> desc
                    ? TypeSafeSorts.descWithId(ClienteModel.class, ClienteModel::getCodigo, ClienteModel::getId)
                    : TypeSafeSorts.ascWithId(ClienteModel.class, ClienteModel::getCodigo, ClienteModel::getId);
            case "clasificacion" -> desc
                    ? TypeSafeSorts.descWithId(ClienteModel.class, ClienteModel::getClasificacion, ClienteModel::getId)
                    : TypeSafeSorts.ascWithId(ClienteModel.class, ClienteModel::getClasificacion, ClienteModel::getId);
            case "razonsocial" -> desc
                    ? TypeSafeSorts.descWithId(ClienteModel.class, ClienteModel::getRazonSocial, ClienteModel::getId)
                    : TypeSafeSorts.ascWithId(ClienteModel.class, ClienteModel::getRazonSocial, ClienteModel::getId);
            default -> desc
                    ? TypeSafeSorts.descWithId(ClienteModel.class, ClienteModel::getNombre, ClienteModel::getId)
                    : TypeSafeSorts.ascWithId(ClienteModel.class, ClienteModel::getNombre, ClienteModel::getId);
        };
    }
}
