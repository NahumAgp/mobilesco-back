package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
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
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.security.RefreshTokenCookieService;
import com.mobilesco.mobilesco_back.modules.auth.application.usecases.AccesoService;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping(ApiPaths.AUTH)
public class AuthController {

    private final AuthService authService;
    private final AccesoService accesoService;
    private final RefreshTokenCookieService cookieService;
    private final boolean legacyResponseEnabled;

    public AuthController(
            AuthService authService,
            AccesoService accesoService,
            RefreshTokenCookieService cookieService,
            @Value("${security.refresh-token.legacy-response-enabled:false}") boolean legacyResponseEnabled) {
        this.authService = authService;
        this.accesoService = accesoService;
        this.cookieService = cookieService;
        this.legacyResponseEnabled = legacyResponseEnabled;
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(String refreshToken) {}
    public record LogoutRequest(String refreshToken) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TokenResponse(String accessToken, String refreshToken) {}

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthService.TokenPair tokens = authService.login(request.email(), request.password());
        cookieService.set(response, tokens.refreshToken());
        return tokenResponse(tokens);
    }

    @PostMapping("/register")
    public ResponseEntity<RegistroInvitacionResponseDTO> register(
            @Valid @RequestBody RegistroInvitacionRequestDTO request
    ) {
        return ResponseEntity.status(201).body(accesoService.registrar(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody(required = false) RefreshRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        String cookieToken = cookieService.readRefreshToken(request);
        String refreshToken = cookieToken;
        if (cookieToken != null) {
            cookieService.validateCsrf(request);
        } else if (body != null) {
            refreshToken = body.refreshToken();
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new org.springframework.security.authentication.BadCredentialsException("Refresh token requerido");
        }

        AuthService.TokenPair tokens = authService.refresh(refreshToken);
        cookieService.set(response, tokens.refreshToken());
        return tokenResponse(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        String cookieToken = cookieService.readRefreshToken(request);
        String refreshToken = cookieToken;
        if (cookieToken != null) {
            cookieService.validateCsrf(request);
        } else if (body != null) {
            refreshToken = body.refreshToken();
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        cookieService.clear(response);
        return ResponseEntity.noContent().build();
    }

    private TokenResponse toResponse(AuthService.TokenPair tokens) {
        return new TokenResponse(
                tokens.accessToken(),
                legacyResponseEnabled ? tokens.refreshToken() : null);
    }

    private ResponseEntity<TokenResponse> tokenResponse(AuthService.TokenPair tokens) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(toResponse(tokens));
    }
   
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> me(Authentication auth) {
        return ResponseEntity.ok(authService.me(auth.getName()));
    }
}
