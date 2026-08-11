package com.mobilesco.mobilesco_back.security;

import java.io.IOException;
import java.util.LinkedHashSet;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.AccesoService;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final AccesoService accesoService;

    public JwtAuthFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            AccesoService accesoService) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.accesoService = accesoService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", seguimos sin autenticar
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();

        try {
            Jws<Claims> parsed = jwtService.parse(token);
            Claims claims = parsed.getBody();

            Long usuarioId = Long.valueOf(claims.getSubject());
            UsuarioModel usuario = usuarioRepository.findOneWithAccessById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario del token no encontrado"));
            if (!cuentaActiva(usuario)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            var authorities = new LinkedHashSet<SimpleGrantedAuthority>();
            usuario.getRoles().stream()
                    .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getName()))
                    .forEach(authorities::add);
            accesoService.obtenerPermisosEfectivos(usuario).stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);

            // El JWT prueba la identidad; el estado y los accesos vigentes vienen de BD.
            var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            // Token inválido o expirado: no autenticamos
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean cuentaActiva(UsuarioModel usuario) {
        return usuario.getEstadoCuenta() == EstadoCuentaUsuario.ACTIVE
                && usuario.isEnabled()
                && !usuario.isLocked();
    }
}
