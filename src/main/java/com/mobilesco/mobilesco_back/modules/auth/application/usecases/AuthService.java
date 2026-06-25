package com.mobilesco.mobilesco_back.modules.auth.application.usecases;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.MeResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.security.JwtService;

@Service
public class AuthService {

    public record TokenPair(String accessToken, String refreshToken) {}

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AccesoService accesoService;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository userRepository,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       AccesoService accesoService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.accesoService = accesoService;
    }

    public TokenPair login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, password)
        );

        UsuarioModel user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String access = jwtService.generateAccessToken(user, accesoService.obtenerPermisosEfectivos(user));
        String refresh = refreshTokenService.issue(user);

        return new TokenPair(access, refresh);
    }

    public TokenPair refresh(String refreshToken) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotate(refreshToken);

        UsuarioModel user = userRepository.findById(result.userId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String newAccess = jwtService.generateAccessToken(user, accesoService.obtenerPermisosEfectivos(user));

        return new TokenPair(newAccess, result.newRefreshToken());
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    public MeResponseDTO me(String email) {
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
        dto.setPermisos(accesoService.obtenerPermisosEfectivos(usuario).stream().sorted().toList());

        if (usuario.getEmpleado() != null) {
            var emp = usuario.getEmpleado();
            dto.setIdEmpleado(emp.getId());
            dto.setNombre(emp.getNombre());
            dto.setApellidoPaterno(emp.getApellidoPaterno());
            dto.setApellidoMaterno(emp.getApellidoMaterno());
            dto.setTelefono(emp.getTelefono());
            dto.setFotoUrl(emp.getFotoUrl());
        }

        return dto;
    }
}
