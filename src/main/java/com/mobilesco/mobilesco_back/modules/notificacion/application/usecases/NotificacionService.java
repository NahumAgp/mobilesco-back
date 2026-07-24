package com.mobilesco.mobilesco_back.modules.notificacion.application.usecases;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.NotificacionModel;
import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.TipoNotificacion;
import com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.in.api.dtos.NotificacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.out.persistence.repositories.DestinatarioNotificacionRepository;
import com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.out.persistence.repositories.NotificacionRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final DestinatarioNotificacionRepository destinatarioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void notificarUsuario(
            UsuarioModel destinatario,
            TipoNotificacion tipo,
            String titulo,
            String mensaje,
            String modulo,
            String entidadTipo,
            Long entidadId,
            String ruta) {
        if (destinatario == null || !destinatario.isEnabled() || destinatario.isLocked()) {
            return;
        }
        notificacionRepository.save(crear(
                destinatario, tipo, titulo, mensaje, modulo, entidadTipo, entidadId, ruta));
    }

    @Transactional
    public int notificarRoles(
            Set<String> roles,
            TipoNotificacion tipo,
            String titulo,
            String mensaje,
            String modulo,
            String entidadTipo,
            Long entidadId,
            String ruta) {
        if (roles == null || roles.isEmpty()) {
            return 0;
        }
        List<NotificacionModel> notificaciones = destinatarioRepository.buscarActivosPorRoles(roles)
                .stream()
                .map(usuario -> crear(usuario, tipo, titulo, mensaje, modulo, entidadTipo, entidadId, ruta))
                .toList();
        notificacionRepository.saveAll(notificaciones);
        return notificaciones.size();
    }

    @Transactional(readOnly = true)
    public Page<NotificacionResponseDTO> listarPropias(
            String email,
            Boolean leida,
            Pageable pageable) {
        Long usuarioId = obtenerUsuario(email).getId();
        return notificacionRepository.listarPropias(usuarioId, leida, pageable).map(this::map);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String email) {
        return notificacionRepository.countByDestinatarioIdAndLeidaFalse(obtenerUsuario(email).getId());
    }

    @Transactional
    public NotificacionResponseDTO marcarLeida(Long id, String email) {
        UsuarioModel usuario = obtenerUsuario(email);
        NotificacionModel notificacion = notificacionRepository
                .findByIdAndDestinatarioId(id, usuario.getId())
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada"));
        if (!Boolean.TRUE.equals(notificacion.getLeida())) {
            notificacion.setLeida(true);
            notificacion.setFechaLectura(LocalDateTime.now());
            notificacion = notificacionRepository.save(notificacion);
        }
        return map(notificacion);
    }

    @Transactional
    public int marcarTodasLeidas(String email) {
        return notificacionRepository.marcarTodasLeidas(obtenerUsuario(email).getId());
    }

    private NotificacionModel crear(
            UsuarioModel destinatario,
            TipoNotificacion tipo,
            String titulo,
            String mensaje,
            String modulo,
            String entidadTipo,
            Long entidadId,
            String ruta) {
        if (!StringUtils.hasText(titulo) || !StringUtils.hasText(mensaje)) {
            throw new BadRequestException("La notificación requiere título y mensaje");
        }
        NotificacionModel notificacion = new NotificacionModel();
        notificacion.setDestinatario(destinatario);
        notificacion.setTipo(tipo == null ? TipoNotificacion.INFORMACION : tipo);
        notificacion.setTitulo(titulo.trim());
        notificacion.setMensaje(mensaje.trim());
        notificacion.setModulo(limpiar(modulo));
        notificacion.setEntidadTipo(limpiar(entidadTipo));
        notificacion.setEntidadId(entidadId);
        notificacion.setRuta(validarRuta(ruta));
        notificacion.setLeida(false);
        return notificacion;
    }

    private String validarRuta(String ruta) {
        String limpia = limpiar(ruta);
        if (limpia == null) {
            return null;
        }
        if (!limpia.startsWith("/") || limpia.startsWith("//") || limpia.contains("://")) {
            throw new BadRequestException("La ruta de la notificación debe ser interna");
        }
        return limpia;
    }

    private UsuarioModel obtenerUsuario(String email) {
        return usuarioRepository.findOneByEmail(email)
                .orElseThrow(() -> new BadRequestException("No se pudo identificar al usuario autenticado"));
    }

    private NotificacionResponseDTO map(NotificacionModel notificacion) {
        return NotificacionResponseDTO.builder()
                .id(notificacion.getId())
                .tipo(notificacion.getTipo())
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .modulo(notificacion.getModulo())
                .entidadTipo(notificacion.getEntidadTipo())
                .entidadId(notificacion.getEntidadId())
                .ruta(notificacion.getRuta())
                .leida(notificacion.getLeida())
                .fechaCreacion(notificacion.getFechaCreacion())
                .fechaLectura(notificacion.getFechaLectura())
                .build();
    }

    private String limpiar(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
