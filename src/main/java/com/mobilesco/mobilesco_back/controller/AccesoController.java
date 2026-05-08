package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.auth.AccesoService;
import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.auth.InvitacionUsuarioCreateDTO;
import com.mobilesco.mobilesco_back.dto.auth.InvitacionUsuarioResponseDTO;
import com.mobilesco.mobilesco_back.dto.auth.UsuarioPendienteResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.AUTH)
public class AccesoController {

    private final AccesoService accesoService;

    public AccesoController(AccesoService accesoService) {
        this.accesoService = accesoService;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL')")
    public ResponseEntity<List<String>> roles() {
        return ResponseEntity.ok(accesoService.listarRolesDisponibles());
    }

    @PostMapping("/invitaciones")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL')")
    public ResponseEntity<InvitacionUsuarioResponseDTO> crearInvitacion(
            @Valid @RequestBody InvitacionUsuarioCreateDTO dto,
            Authentication authentication
    ) {
        String creador = authentication != null ? authentication.getName() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(accesoService.crearInvitacion(dto, creador));
    }

    @GetMapping("/usuarios-pendientes")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL')")
    public ResponseEntity<List<UsuarioPendienteResponseDTO>> pendientes() {
        return ResponseEntity.ok(accesoService.listarPendientes());
    }

    @PostMapping("/usuarios-pendientes/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL')")
    public ResponseEntity<UsuarioPendienteResponseDTO> aprobar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accesoService.aprobarUsuario(id, authentication.getName()));
    }
}
