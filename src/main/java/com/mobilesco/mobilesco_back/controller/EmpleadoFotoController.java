package com.mobilesco.mobilesco_back.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.services.AlmacenamientoImagenesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Empleado - Foto", description = "Subida de foto de perfil de empleados")
@RestController
@RequestMapping(ApiPaths.EMPLEADOS)
public class EmpleadoFotoController {

    private final EmpleadoRepository empleadoRepository;
    private final AlmacenamientoImagenesService almacenamientoImagenesService;

    public EmpleadoFotoController(
            EmpleadoRepository empleadoRepository,
            AlmacenamientoImagenesService almacenamientoImagenesService
    ) {
        this.empleadoRepository = empleadoRepository;
        this.almacenamientoImagenesService = almacenamientoImagenesService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/foto")
    @Operation(summary = "Subir/reemplazar foto de un empleado (ADMIN)")
    public ResponseEntity<?> subirFotoEmpleado(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo
    ) throws IOException {

        EmpleadoModel empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        String rutaPublica = almacenamientoImagenesService.guardarFotoPerfilEmpleado(id, archivo);

        empleado.setFotoUrl(rutaPublica);
        empleadoRepository.save(empleado);

        return ResponseEntity.ok(Map.of("fotoUrl", rutaPublica));
    }
}