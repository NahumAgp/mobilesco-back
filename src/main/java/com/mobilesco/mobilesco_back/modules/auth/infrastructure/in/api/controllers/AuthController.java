package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.AuthService;
import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.MeResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RegistroInvitacionRequestDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RegistroInvitacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.application.usecases.AccesoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping(ApiPaths.AUTH)
public class AuthController {

    private final AuthService authService;
    private final AccesoService accesoService;

    public AuthController(AuthService authService, AccesoService accesoService) {
        this.authService = authService;
        this.accesoService = accesoService;
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record LogoutRequest(@NotBlank String refreshToken) {}

    @PostMapping("/login")
    public ResponseEntity<AuthService.TokenPair> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.email(), request.password()));
    }

    @PostMapping("/register")
    public ResponseEntity<RegistroInvitacionResponseDTO> register(
            @Valid @RequestBody RegistroInvitacionRequestDTO request
    ) {
        return ResponseEntity.status(201).body(accesoService.registrar(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthService.TokenPair> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
   
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> me(Authentication auth) {
        return ResponseEntity.ok(authService.me(auth.getName()));
    }
}
