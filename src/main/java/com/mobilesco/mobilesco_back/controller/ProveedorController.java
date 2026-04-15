package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import com.mobilesco.mobilesco_back.dto.proveedor.ProveedorCreateDTO;
import com.mobilesco.mobilesco_back.dto.proveedor.ProveedorResponseDTO;
import com.mobilesco.mobilesco_back.dto.proveedor.ProveedorUpdateDTO;
import com.mobilesco.mobilesco_back.enums.TipoInsumo;
import com.mobilesco.mobilesco_back.services.ProveedorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Proveedor", description = "CRUD y gestión de proveedores")
@RestController
@RequestMapping(ApiPaths.PROVEEDORES)
public class ProveedorController {  // ✅ Nombre correcto con 'e'

    private final ProveedorService proveedorService;  // ✅ Nombre correcto con 'e'

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    // =====================================================
    // 🔹 LISTAR
    // =====================================================

    @GetMapping
    @Operation(summary = "Listar proveedores")
    public ResponseEntity<List<ProveedorResponseDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String nombre
    ) {

        boolean tieneNombre = (nombre != null && !nombre.isBlank());

        if (activo != null && tieneNombre) {
            return ResponseEntity.ok(
                    proveedorService.buscarPorActivoYNombre(activo, nombre)
            );
        }

        if (activo != null) {
            return ResponseEntity.ok(
                    proveedorService.buscarPorActivo(activo)
            );
        }

        if (tieneNombre) {
            return ResponseEntity.ok(
                    proveedorService.buscarPorNombre(nombre)
            );
        }

        return ResponseEntity.ok(
                proveedorService.obtenerTodos()
        );
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
    @PreAuthorize("hasRole('ADMIN')")
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proveedor")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        proveedorService.eliminar(id);  // ✅ Nombre correcto

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // 🔹 TIPOS DE INSUMO (UTILIDAD)
    // =====================================================

    @GetMapping("/tipos-insumo")
    @Operation(summary = "Obtener todos los tipos de insumo")
    public ResponseEntity<TipoInsumo[]> getTiposInsumo() {
        TipoInsumo[] tipos = proveedorService.getTodosLosTipos();  // ✅ Nombre correcto
        return ResponseEntity.ok(tipos);
    }

    // =====================================================
    // 🔹 BUSCAR POR TIPO DE INSUMO (CORREGIDO)
    // =====================================================

    @GetMapping("/por-tipo/{tipo}")
    @Operation(summary = "Buscar proveedores por tipo de insumo")
    public ResponseEntity<List<ProveedorResponseDTO>> getProveedoresPorTipo(@PathVariable TipoInsumo tipo) {
        List<ProveedorResponseDTO> proveedores = proveedorService.getProveedoresPorTipo(tipo);  // ✅ Método corregido
        return ResponseEntity.ok(proveedores);
    }
}