package com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.application.usecases.TipoInsumoService;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoCodigoPreviewDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoEstadoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoUpdateDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Tipos de insumo", description = "Catalogo administrable de tipos de insumo")
@RestController
@RequestMapping(ApiPaths.TIPOS_INSUMO)
public class TipoInsumoController {

    private final TipoInsumoService tipoInsumoService;

    public TipoInsumoController(TipoInsumoService tipoInsumoService) {
        this.tipoInsumoService = tipoInsumoService;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de insumo")
    public ResponseEntity<?> listar(
            @RequestParam(defaultValue = "false") boolean soloActivos,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        if (page != null) {
            int pageNumber = Math.max(page, 0);
            int pageSize = size == null ? 10 : Math.max(1, Math.min(size, 100));
            PageRequest pageable = PageRequest.of(
                    pageNumber,
                    pageSize,
                    TypeSafeSorts.desc(TipoInsumoModel.class, TipoInsumoModel::getActivo)
                            .and(TypeSafeSorts.ascWithId(TipoInsumoModel.class, TipoInsumoModel::getNombre, TipoInsumoModel::getId)));
            return ResponseEntity.ok(tipoInsumoService.listarPaginado(soloActivos, busqueda, pageable));
        }
        return ResponseEntity.ok(tipoInsumoService.listar(soloActivos));
    }

    @GetMapping("/preview-codigo")
    @Operation(summary = "Previsualizar codigo sugerido para un tipo de insumo")
    public ResponseEntity<TipoInsumoCodigoPreviewDTO> previsualizarCodigo(
            @RequestParam String nombre
    ) {
        return ResponseEntity.ok(tipoInsumoService.previsualizarCodigo(nombre));
    }

    @PreAuthorize("hasAuthority('VIEW_INPUT_TYPES') and hasAuthority('ACTION_INPUT_TYPES_CREATE')")
    @PostMapping
    @Operation(summary = "Crear tipo de insumo")
    public ResponseEntity<TipoInsumoResponseDTO> crear(
            @Valid @RequestBody TipoInsumoCreateDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoInsumoService.crear(dto));
    }

    @PreAuthorize("hasAuthority('VIEW_INPUT_TYPES') and hasAuthority('ACTION_INPUT_TYPES_EDIT')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de insumo")
    public ResponseEntity<TipoInsumoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoInsumoUpdateDTO dto
    ) {
        return ResponseEntity.ok(tipoInsumoService.actualizar(id, dto));
    }

    @PreAuthorize("hasAuthority('VIEW_INPUT_TYPES') and hasAuthority('ACTION_INPUT_TYPES_STATUS')")
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar tipo de insumo")
    public ResponseEntity<TipoInsumoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody TipoInsumoEstadoUpdateDTO dto
    ) {
        return ResponseEntity.ok(tipoInsumoService.actualizarEstado(id, dto.getActivo()));
    }
}
