package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.AccesoService;
import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.InvitacionUsuarioCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.InvitacionUsuarioResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.PermisoResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolUpdateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioAccesoResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioAccesoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioPendienteResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.AUTH)
public class AccesoController {

    private final AccesoService accesoService;

    public AccesoController(AccesoService accesoService) {
        this.accesoService = accesoService;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    public ResponseEntity<List<String>> roles() {
        return ResponseEntity.ok(accesoService.listarRolesDisponibles());
    }

    @GetMapping("/permisos")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('VIEW_USERS')")
    public ResponseEntity<List<PermisoResponseDTO>> permisos() {
        return ResponseEntity.ok(accesoService.listarPermisos());
    }

    @GetMapping("/roles-config")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('VIEW_USERS')")
    public ResponseEntity<List<RolResponseDTO>> rolesConfig() {
        return ResponseEntity.ok(accesoService.listarRolesDetalle());
    }

    @PostMapping("/roles-config")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<RolResponseDTO> crearRol(@Valid @RequestBody RolCreateDTO dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accesoService.crearRol(dto, authentication.getName()));
    }

    @PatchMapping("/roles-config/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<RolResponseDTO> actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody RolUpdateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accesoService.actualizarRol(id, dto, authentication.getName()));
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('VIEW_USERS')")
    public ResponseEntity<List<UsuarioAccesoResponseDTO>> usuarios() {
        return ResponseEntity.ok(accesoService.listarUsuarios());
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<UsuarioAccesoResponseDTO> crearUsuario(
            @Valid @RequestBody UsuarioCreateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accesoService.crearUsuario(dto, authentication.getName()));
    }

    @PatchMapping("/usuarios/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<UsuarioAccesoResponseDTO> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAccesoUpdateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accesoService.actualizarUsuario(id, dto, authentication.getName()));
    }

    @PostMapping("/usuarios/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<UsuarioAccesoResponseDTO> desactivarUsuario(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accesoService.desactivarUsuario(id, authentication.getName()));
    }

    @PostMapping("/invitaciones")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    public ResponseEntity<InvitacionUsuarioResponseDTO> crearInvitacion(
            @Valid @RequestBody InvitacionUsuarioCreateDTO dto,
            Authentication authentication
    ) {
        String creador = authentication != null ? authentication.getName() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(accesoService.crearInvitacion(dto, creador));
    }

    @GetMapping("/usuarios-pendientes")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    public ResponseEntity<List<UsuarioPendienteResponseDTO>> pendientes() {
        return ResponseEntity.ok(accesoService.listarPendientes());
    }

    @PostMapping("/usuarios-pendientes/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    public ResponseEntity<UsuarioPendienteResponseDTO> aprobar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accesoService.aprobarUsuario(id, authentication.getName()));
    }
}
