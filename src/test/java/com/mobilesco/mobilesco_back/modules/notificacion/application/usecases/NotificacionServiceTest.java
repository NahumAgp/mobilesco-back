package com.mobilesco.mobilesco_back.modules.notificacion.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.NotificacionModel;
import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.TipoNotificacion;
import com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.out.persistence.repositories.DestinatarioNotificacionRepository;
import com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.out.persistence.repositories.NotificacionRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private DestinatarioNotificacionRepository destinatarioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private NotificacionService service;

    @BeforeEach
    void setUp() {
        service = new NotificacionService(
                notificacionRepository,
                destinatarioRepository,
                usuarioRepository);
    }

    @Test
    void notificarRolesGeneraUnaCopiaPorUsuarioActivo() {
        UsuarioModel uno = usuario(1L, "uno@mobilesco.mx", true, false);
        UsuarioModel dos = usuario(2L, "dos@mobilesco.mx", true, false);
        when(destinatarioRepository.buscarActivosPorRoles(Set.of("ROL_A", "ROL_B")))
                .thenReturn(List.of(uno, dos));

        int creadas = service.notificarRoles(
                Set.of("ROL_A", "ROL_B"),
                TipoNotificacion.ACCION_REQUERIDA,
                "Acción",
                "Revisa el registro",
                "ALMACEN",
                "REQUISICION",
                10L,
                "/almacen/requisiciones/10");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NotificacionModel>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificacionRepository).saveAll(captor.capture());
        assertEquals(2, creadas);
        assertEquals(2, captor.getValue().size());
        assertEquals(2, captor.getValue().stream().map(n -> n.getDestinatario().getId()).distinct().count());
    }

    @Test
    void noNotificaUsuarioDeshabilitado() {
        UsuarioModel usuario = usuario(3L, "inactivo@mobilesco.mx", false, false);

        service.notificarUsuario(
                usuario,
                TipoNotificacion.INFORMACION,
                "Aviso",
                "Mensaje",
                "SISTEMA",
                null,
                null,
                null);

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void rechazaRutasExternas() {
        UsuarioModel usuario = usuario(4L, "activo@mobilesco.mx", true, false);

        assertThrows(BadRequestException.class, () -> service.notificarUsuario(
                usuario,
                TipoNotificacion.INFORMACION,
                "Aviso",
                "Mensaje",
                "SISTEMA",
                null,
                null,
                "https://sitio-externo.test"));
    }

    @Test
    void marcarLeidaBuscaPorIdYDestinatario() {
        UsuarioModel usuario = usuario(8L, "persona@mobilesco.mx", true, false);
        NotificacionModel notificacion = new NotificacionModel();
        notificacion.setId(15L);
        notificacion.setDestinatario(usuario);
        notificacion.setTipo(TipoNotificacion.INFORMACION);
        notificacion.setTitulo("Aviso");
        notificacion.setMensaje("Mensaje");
        notificacion.setLeida(false);
        when(usuarioRepository.findOneByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(notificacionRepository.findByIdAndDestinatarioId(15L, 8L))
                .thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(notificacion)).thenReturn(notificacion);

        var resultado = service.marcarLeida(15L, usuario.getEmail());

        assertEquals(true, resultado.getLeida());
        verify(notificacionRepository).findByIdAndDestinatarioId(15L, 8L);
    }

    private UsuarioModel usuario(Long id, String email, boolean enabled, boolean locked) {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(email);
        usuario.setEnabled(enabled);
        usuario.setLocked(locked);
        try {
            var field = UsuarioModel.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(usuario, id);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        return usuario;
    }
}
