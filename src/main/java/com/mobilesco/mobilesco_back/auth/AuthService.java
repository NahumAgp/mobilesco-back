package com.mobilesco.mobilesco_back.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.auth.MeResponseDTO;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.UsuarioModel;
import com.mobilesco.mobilesco_back.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.security.JwtService;

@Service
public class AuthService {

    public record TokenPair(String accessToken, String refreshToken) {}

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository userRepository,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public TokenPair login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UsuarioModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String access = jwtService.generateAccessToken(user);
        String refresh = refreshTokenService.issue(user);

        return new TokenPair(access, refresh);
    }

    public TokenPair refresh(String refreshToken) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotate(refreshToken);

        UsuarioModel user = userRepository.findById(result.userId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String newAccess = jwtService.generateAccessToken(user);

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
