package com.mobilesco.mobilesco_back.controller;

import java.io.IOException;
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
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.models.UsuarioModel;
import com.mobilesco.mobilesco_back.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.services.AlmacenamientoImagenesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Empleado - Mi Foto", description = "El empleado autenticado sube su propia foto")
@RestController
@RequestMapping(ApiPaths.EMPLEADOS) // /api/v1/empleados
public class EmpleadoMeFotoController {

    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AlmacenamientoImagenesService almacenamientoImagenesService;

    public EmpleadoMeFotoController(
            UsuarioRepository usuarioRepository,
            EmpleadoRepository empleadoRepository,
            AlmacenamientoImagenesService almacenamientoImagenesService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.almacenamientoImagenesService = almacenamientoImagenesService;
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
    ) throws IOException {

        // 1) Del token: tu JwtAuthFilter puso el principal como el email (String)
        String email = auth.getName();

        // 2) Buscar usuario por email
        UsuarioModel usuario = usuarioRepository.findOneByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // 3) Validar que el usuario tenga empleado asociado
        EmpleadoModel empleado = usuario.getEmpleado();
        if (empleado == null) {
            throw new BadRequestException("Tu usuario no tiene un empleado asociado");
        }

        // 4) Guardar archivo en disco y obtener ruta pública /uploads/...
        String rutaPublica = almacenamientoImagenesService.guardarFotoPerfilEmpleado(empleado.getId(), archivo);

        // 5) Guardar URL en BD
        empleado.setFotoUrl(rutaPublica);
        empleadoRepository.save(empleado);

        return ResponseEntity.ok(Map.of("fotoUrl", rutaPublica));
    }
}