package com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos.ProveedorCreateDTO;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos.ProveedorResponseDTO;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos.ProveedorUpdateDTO;
import com.mobilesco.mobilesco_back.modules.proveedor.application.usecases.ProveedorService;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;
import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Proveedor", description = "CRUD y gestión de proveedores")
@RestController
@RequestMapping(ApiPaths.PROVEEDORES)
public class ProveedorController {  // ✅ Nombre correcto con 'e'

    private static final int PAGE_SIZE = 10;

    private final ProveedorService proveedorService;  // ✅ Nombre correcto con 'e'

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    // =====================================================
    // 🔹 LISTAR
    // =====================================================

    @GetMapping
    @Operation(summary = "Listar proveedores")
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String tipoInsumo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        if (page != null || size != null) {
            int pageNumber = page != null ? page : 0;
            int pageSize = size != null ? size : PAGE_SIZE;
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Sort sort = switch (sortBy == null ? "id" : sortBy.trim().toLowerCase()) {
                case "id" -> sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descById(ProveedorModel.class, ProveedorModel::getId)
                        : TypeSafeSorts.ascById(ProveedorModel.class, ProveedorModel::getId);
                case "razonsocial", "razon_social" -> sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(ProveedorModel.class, ProveedorModel::getRazonSocial, ProveedorModel::getId)
                        : TypeSafeSorts.ascWithId(ProveedorModel.class, ProveedorModel::getRazonSocial, ProveedorModel::getId);
                case "nombre" -> sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(ProveedorModel.class, ProveedorModel::getNombre, ProveedorModel::getId)
                        : TypeSafeSorts.ascWithId(ProveedorModel.class, ProveedorModel::getNombre, ProveedorModel::getId);
                case "apellidopaterno", "apellido_paterno" -> sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(ProveedorModel.class, ProveedorModel::getApellidoPaterno, ProveedorModel::getId)
                        : TypeSafeSorts.ascWithId(ProveedorModel.class, ProveedorModel::getApellidoPaterno, ProveedorModel::getId);
                case "tipodeinsumo", "tipo_insumo" -> sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(ProveedorModel.class, ProveedorModel::getTipoInsumo, ProveedorModel::getId)
                        : TypeSafeSorts.ascWithId(ProveedorModel.class, ProveedorModel::getTipoInsumo, ProveedorModel::getId);
                case "activo" -> sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(ProveedorModel.class, ProveedorModel::getActivo, ProveedorModel::getId)
                        : TypeSafeSorts.ascWithId(ProveedorModel.class, ProveedorModel::getActivo, ProveedorModel::getId);
                default -> sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descById(ProveedorModel.class, ProveedorModel::getId)
                        : TypeSafeSorts.ascById(ProveedorModel.class, ProveedorModel::getId);
            };
            PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);
            Page<ProveedorResponseDTO> proveedores = proveedorService.buscarFiltradoPaginado(activo, tipoInsumo, busqueda, pageable);
            return ResponseEntity.ok(proveedores);
        }

        return ResponseEntity.ok(proveedorService.buscarFiltrado(activo, tipoInsumo, busqueda));
    }

    // =====================================================
    // 🔹 REPORTE EXCEL
    // =====================================================

    @GetMapping("/reporte/excel")
    @Operation(summary = "Exportar proveedores a Excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String tipoInsumo,
            @RequestParam(required = false) String busqueda
    ) {
        byte[] excel = proveedorService.generarReporteExcel(activo, tipoInsumo, busqueda);
        String filename = "proveedores.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    // =====================================================
    // 🔹 OBTENER POR ID
    // =====================================================

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por ID")
    public ResponseEntity<ProveedorResponseDTO> obtenerPorId(@PathVariable Long id) {

        ProveedorResponseDTO proveedor = proveedorService.obtenerPorId(id);

        return ResponseEntity.ok(proveedor);
    }

    // =====================================================
    // 🔹 CREAR
    // =====================================================
    @PreAuthorize("hasAuthority('VIEW_SUPPLIERS') and hasAuthority('ACTION_SUPPLIERS_CREATE')")
    @PostMapping
    public ResponseEntity<ProveedorResponseDTO> crear(
            @Valid @RequestBody ProveedorCreateDTO dto) {

        ProveedorResponseDTO creado = proveedorService.crear(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    // =====================================================
    // 🔹 ACTUALIZAR
    // =====================================================

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor")
    public ResponseEntity<ProveedorResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorUpdateDTO dto
    ) {

        ProveedorResponseDTO actualizado =
                proveedorService.actualizar(id, dto);

        return ResponseEntity.ok(actualizado);
    }

    // =====================================================
    // 🔹 ELIMINAR
    // =====================================================

    @PreAuthorize("hasAuthority('VIEW_SUPPLIERS') and hasAuthority('ACTION_SUPPLIERS_DELETE')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proveedor")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        proveedorService.eliminar(id);  // ✅ Nombre correcto

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/por-tipo/{tipo}")
    @Operation(summary = "Buscar proveedores por tipo de insumo")
    public ResponseEntity<List<ProveedorResponseDTO>> getProveedoresPorTipo(@PathVariable String tipo) {
        List<ProveedorResponseDTO> proveedores = proveedorService.getProveedoresPorTipo(tipo);  // ✅ Método corregido
        return ResponseEntity.ok(proveedores);
    }
}
