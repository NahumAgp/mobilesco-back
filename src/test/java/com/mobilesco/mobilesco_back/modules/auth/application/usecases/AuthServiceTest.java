package com.mobilesco.mobilesco_back.modules.auth.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.security.JwtService;

class AuthServiceTest {

    private UsuarioRepository usuarioRepository;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private AccesoService accesoService;
    private AuthService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        jwtService = mock(JwtService.class);
        refreshTokenService = mock(RefreshTokenService.class);
        accesoService = mock(AccesoService.class);
        service = new AuthService(
                mock(AuthenticationManager.class),
                usuarioRepository,
                jwtService,
                refreshTokenService,
                accesoService);
    }

    @Test
    void refreshEmiteTokenConPermisosVigentesParaCuentaActiva() {
        UsuarioModel usuario = usuario(EstadoCuentaUsuario.ACTIVE, true, false);
        prepararRotacion(usuario);
        when(accesoService.obtenerPermisosEfectivos(usuario)).thenReturn(Set.of("VIEW_DASHBOARD"));
        when(jwtService.generateAccessToken(usuario, Set.of("VIEW_DASHBOARD"))).thenReturn("access-nuevo");

        AuthService.TokenPair resultado = service.refresh("refresh-anterior");

        assertEquals("access-nuevo", resultado.accessToken());
        assertEquals("refresh-nuevo", resultado.refreshToken());
        verify(refreshTokenService, never()).revoke("refresh-nuevo");
    }

    @Test
    void refreshRechazaYRevocaSucesorSiLaCuentaEstaDeshabilitada() {
        UsuarioModel usuario = usuario(EstadoCuentaUsuario.ACTIVE, false, false);
        prepararRotacion(usuario);

        assertThrows(DisabledException.class, () -> service.refresh("refresh-anterior"));

        verify(refreshTokenService).revoke("refresh-nuevo");
        verifyNoInteractions(jwtService, accesoService);
    }

    @Test
    void refreshRechazaYRevocaSucesorSiLaCuentaEstaBloqueada() {
        UsuarioModel usuario = usuario(EstadoCuentaUsuario.ACTIVE, true, true);
        prepararRotacion(usuario);

        assertThrows(LockedException.class, () -> service.refresh("refresh-anterior"));

        verify(refreshTokenService).revoke("refresh-nuevo");
        verifyNoInteractions(jwtService, accesoService);
    }

    @Test
    void refreshRechazaYRevocaSucesorSiElEstadoNoEsActivo() {
        UsuarioModel usuario = usuario(EstadoCuentaUsuario.SUSPENDED, true, false);
        prepararRotacion(usuario);

        assertThrows(LockedException.class, () -> service.refresh("refresh-anterior"));

        verify(refreshTokenService).revoke("refresh-nuevo");
        verifyNoInteractions(jwtService, accesoService);
    }

    private void prepararRotacion(UsuarioModel usuario) {
        when(refreshTokenService.rotate("refresh-anterior"))
                .thenReturn(new RefreshTokenService.RotationResult(42L, "refresh-nuevo"));
        when(usuarioRepository.findOneWithAccessById(42L)).thenReturn(Optional.of(usuario));
    }

    private UsuarioModel usuario(EstadoCuentaUsuario estado, boolean enabled, boolean locked) {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail("usuario@mobilesco.test");
        usuario.setEstadoCuenta(estado);
        usuario.setEnabled(enabled);
        usuario.setLocked(locked);
        return usuario;
    }
}
