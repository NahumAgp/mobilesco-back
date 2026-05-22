package com.mobilesco.mobilesco_back.modules.empleado.infrastructure.in.api.controllers;

import java.util.List;

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
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.in.api.dtos.EmpleadoCreateDTO;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.in.api.dtos.EmpleadoResponseDTO;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.in.api.dtos.EmpleadoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.empleado.application.usecases.EmpleadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Empleado", description = "CRUD y gestión de empleados")
@RestController
@RequestMapping(ApiPaths.EMPLEADOS)
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    // =====================================================
    // 🔹 LISTAR
    // =====================================================

    @GetMapping
    @Operation(summary = "Listar empleados")
    public ResponseEntity<List<EmpleadoResponseDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String nombre
    ) {
        return ResponseEntity.ok(empleadoService.listar(activo, nombre));
    }

    // =====================================================
    // 🔹 OBTENER POR ID
    // =====================================================

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empleado por ID")
    public ResponseEntity<EmpleadoResponseDTO> obtenerPorId(@PathVariable Long id) {
        EmpleadoResponseDTO empleado = empleadoService.obtenerPorId(id);
        return ResponseEntity.ok(empleado);
    }

    // =====================================================
    // 🔹 CREAR
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    @PostMapping
    @Operation(summary = "Crear empleado")
    public ResponseEntity<EmpleadoResponseDTO> crear(
            @Valid @RequestBody EmpleadoCreateDTO dto
    ) {
        EmpleadoResponseDTO creado = empleadoService.crear(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    // =====================================================
    // 🔹 ACTUALIZAR
    // =====================================================

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empleado")
    public ResponseEntity<EmpleadoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoUpdateDTO dto
    ) {
        EmpleadoResponseDTO actualizado = empleadoService.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar empleado")
    public ResponseEntity<EmpleadoResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.cambiarActivo(id, true));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar empleado")
    public ResponseEntity<EmpleadoResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.cambiarActivo(id, false));
    }

    // =====================================================
    // 🔹 ELIMINAR
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empleado")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empleadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
