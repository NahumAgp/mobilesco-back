package com.mobilesco.mobilesco_back.security;

import java.util.stream.Collectors;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository userRepository;

    public CustomUserDetailsService(UsuarioRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring llama a este método cuando alguien intenta autenticarse
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UsuarioModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        EstadoCuentaUsuario estado = user.getEstadoCuenta();
        if (estado == null) {
            estado = user.isEnabled() && !user.isLocked()
                    ? EstadoCuentaUsuario.ACTIVE
                    : EstadoCuentaUsuario.PENDING;
        }
        if (estado != EstadoCuentaUsuario.ACTIVE) {
            throw new LockedException("Cuenta pendiente de aprobacion");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("Cuenta deshabilitada");
        }
        if (user.isLocked()) {
            throw new LockedException("Cuenta bloqueada");
        }

        // Convertimos roles de la BD a "authorities" que entiende Spring
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

        // Esto es el "usuario" que Spring Security usa internamente
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}
