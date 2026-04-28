package com.mobilesco.mobilesco_back.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.services.EmpleadoFotoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Empleado - Mi Foto", description = "El empleado autenticado sube su propia foto")
@RestController
@RequestMapping(ApiPaths.EMPLEADOS) // /api/v1/empleados
public class EmpleadoMeFotoController {

    private final EmpleadoFotoService empleadoFotoService;

    public EmpleadoMeFotoController(EmpleadoFotoService empleadoFotoService) {
        this.empleadoFotoService = empleadoFotoService;
    }

    /**
     * ✅ El empleado autenticado sube su propia foto
     * POST /api/v1/empleados/me/foto
     * form-data: archivo (file)
     */
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')") // si quieres: solo EMPLEADO, deja ('EMPLEADO')
    @PostMapping("/me/foto")
    @Operation(summary = "Subir/reemplazar mi foto (EMPLEADO)")
    public ResponseEntity<?> subirMiFoto(
            Authentication auth,
            @RequestParam("archivo") MultipartFile archivo
    ) {
        String rutaPublica = empleadoFotoService.subirFotoDelEmpleadoActual(auth, archivo);
        return ResponseEntity.ok(Map.of("fotoUrl", rutaPublica));
    }
}
