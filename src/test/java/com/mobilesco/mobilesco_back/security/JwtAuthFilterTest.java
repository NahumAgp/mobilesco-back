package com.mobilesco.mobilesco_back.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.AccesoService;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private AccesoService accesoService;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        accesoService = mock(AccesoService.class);
        filter = new JwtAuthFilter(jwtService, usuarioRepository, accesoService);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usaRolesYPermisosVigentesDeBaseDeDatos() throws Exception {
        tokenConUsuario(42L);
        UsuarioModel usuario = usuarioActivo("actual@mobilesco.test", "ADMIN");
        when(usuarioRepository.findOneWithAccessById(42L)).thenReturn(Optional.of(usuario));
        when(accesoService.obtenerPermisosEfectivos(usuario))
                .thenReturn(Set.of("VIEW_USERS", "ACTION_USER_ROLES"));

        ejecutar();

        Set<String> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
        assertEquals("actual@mobilesco.test", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals(Set.of("ROLE_ADMIN", "VIEW_USERS", "ACTION_USER_ROLES"), authorities);
    }

    @Test
    void noAutenticaUnaCuentaQueYaNoEstaActiva() throws Exception {
        tokenConUsuario(42L);
        UsuarioModel usuario = usuarioActivo("suspendido@mobilesco.test", "EMPLOYEE");
        usuario.setEstadoCuenta(EstadoCuentaUsuario.SUSPENDED);
        when(usuarioRepository.findOneWithAccessById(42L)).thenReturn(Optional.of(usuario));

        ejecutar();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(accesoService);
    }

    private void ejecutar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/tablero");
        request.addHeader("Authorization", "Bearer token-vigente");
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    @SuppressWarnings("unchecked")
    private void tokenConUsuario(Long usuarioId) {
        Jws<Claims> parsed = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jwtService.parse("token-vigente")).thenReturn(parsed);
        when(parsed.getBody()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(usuarioId.toString());
    }

    private UsuarioModel usuarioActivo(String email, String rolNombre) {
        RolModel rol = new RolModel();
        rol.setName(rolNombre);
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(email);
        usuario.setEnabled(true);
        usuario.setLocked(false);
        usuario.setEstadoCuenta(EstadoCuentaUsuario.ACTIVE);
        usuario.setRoles(Set.of(rol));
        return usuario;
    }
}
