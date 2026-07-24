package com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.in.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.notificacion.application.usecases.NotificacionService;
import com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.in.api.dtos.ConteoNotificacionesDTO;
import com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.in.api.dtos.NotificacionResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.NOTIFICACIONES)
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<Page<NotificacionResponseDTO>> listar(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        return ResponseEntity.ok(notificacionService.listarPropias(
                authentication.getName(), leida, pageable));
    }

    @GetMapping("/no-leidas/conteo")
    public ResponseEntity<ConteoNotificacionesDTO> contar(Authentication authentication) {
        return ResponseEntity.ok(new ConteoNotificacionesDTO(
                notificacionService.contarNoLeidas(authentication.getName())));
    }

    @PatchMapping("/{id}/leida")
    public ResponseEntity<NotificacionResponseDTO> marcarLeida(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(notificacionService.marcarLeida(id, authentication.getName()));
    }

    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodas(Authentication authentication) {
        notificacionService.marcarTodasLeidas(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
