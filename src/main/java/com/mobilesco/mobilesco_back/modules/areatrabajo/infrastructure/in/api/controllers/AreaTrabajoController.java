package com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
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
import com.mobilesco.mobilesco_back.modules.areatrabajo.application.usecases.AreaTrabajoService;
import com.mobilesco.mobilesco_back.modules.areatrabajo.domain.models.AreaTrabajoModel;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoCreateDTO;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoResponseDTO;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.AREAS_TRABAJO)
public class AreaTrabajoController {

    private static final String PERMISO_GESTION_AREAS =
            "hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('VIEW_EMPLOYEES')";
    private static final String PERMISO_CONSULTA_AREAS_PARA_SALIDAS =
            PERMISO_GESTION_AREAS + " or hasRole('JEFE_ALMACEN')";

    private final AreaTrabajoService areaTrabajoService;

    public AreaTrabajoController(AreaTrabajoService areaTrabajoService) {
        this.areaTrabajoService = areaTrabajoService;
    }

    @GetMapping
    @PreAuthorize(PERMISO_CONSULTA_AREAS_PARA_SALIDAS)
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Boolean activo,
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
                    TypeSafeSorts.ascWithId(AreaTrabajoModel.class, AreaTrabajoModel::getNombre, AreaTrabajoModel::getId));
            return ResponseEntity.ok(areaTrabajoService.listarPaginado(activo, busqueda, pageable));
        }
        return ResponseEntity.ok(areaTrabajoService.listar(activo));
    }

    @GetMapping("/codigo-sugerido")
    @PreAuthorize(PERMISO_CONSULTA_AREAS_PARA_SALIDAS)
    public ResponseEntity<Map<String, String>> sugerirCodigo(@RequestParam String nombre) {
        return ResponseEntity.ok(Map.of("codigo", areaTrabajoService.sugerirCodigo(nombre)));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PERMISO_CONSULTA_AREAS_PARA_SALIDAS)
    public ResponseEntity<AreaTrabajoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(areaTrabajoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize(PERMISO_CONSULTA_AREAS_PARA_SALIDAS)
    public ResponseEntity<AreaTrabajoResponseDTO> crear(@Valid @RequestBody AreaTrabajoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(areaTrabajoService.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize(PERMISO_GESTION_AREAS)
    public ResponseEntity<AreaTrabajoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AreaTrabajoUpdateDTO dto
    ) {
        return ResponseEntity.ok(areaTrabajoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize(PERMISO_GESTION_AREAS)
    public ResponseEntity<AreaTrabajoResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(areaTrabajoService.cambiarActivo(id, true));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize(PERMISO_GESTION_AREAS)
    public ResponseEntity<AreaTrabajoResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(areaTrabajoService.cambiarActivo(id, false));
    }
}
