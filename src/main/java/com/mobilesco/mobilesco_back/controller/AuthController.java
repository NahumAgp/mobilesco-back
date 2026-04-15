package com.mobilesco.mobilesco_back.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.auth.RefreshTokenService;
import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.auth.MeResponseDTO;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.UsuarioModel;
import com.mobilesco.mobilesco_back.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.security.JwtService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping(ApiPaths.AUTH)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioRepository userRepository,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record LogoutRequest(@NotBlank String refreshToken) {}
    public record TokenResponse(String accessToken, String refreshToken) {}

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {

        // 1) valida email/password (esto usa CustomUserDetailsService + BCrypt)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2) carga usuario para generar token con id/roles
        var user = userRepository.findByEmail(request.email()).orElseThrow();

        // 3) generar tokens
        String access = jwtService.generateAccessToken(user);
        String refresh = refreshTokenService.issue(user);

        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {

        var result = refreshTokenService.rotate(request.refreshToken());

        // Cargar el usuario real desde BD 
        var user = userRepository.findById(result.userId()).orElseThrow();

        String newAccess = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(new TokenResponse(newAccess, result.newRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
   
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> me(Authentication auth) {

        String email = auth.getName();

        UsuarioModel usuario = userRepository.findOneByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        MeResponseDTO dto = new MeResponseDTO();
        dto.setIdUsuario(usuario.getId());
        dto.setCorreo(usuario.getEmail());

        dto.setRoles(
                usuario.getRoles().stream()
                        .map(r -> r.getName())
                        .toList()
        );

        if (usuario.getEmpleado() != null) {
            var emp = usuario.getEmpleado();
            dto.setIdEmpleado(emp.getId());
            dto.setNombre(emp.getNombre());
            dto.setApellidoPaterno(emp.getApellidoPaterno());
            dto.setApellidoMaterno(emp.getApellidoMaterno());
            dto.setTelefono(emp.getTelefono());
            dto.setFotoUrl(emp.getFotoUrl());
        }

        return ResponseEntity.ok(dto);
    }
}
