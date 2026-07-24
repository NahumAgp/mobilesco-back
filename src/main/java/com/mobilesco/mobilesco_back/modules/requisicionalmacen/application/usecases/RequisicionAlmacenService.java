package com.mobilesco.mobilesco_back.modules.requisicionalmacen.application.usecases;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.notificacion.application.usecases.NotificacionService;
import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.TipoNotificacion;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.EstadoRequisicionAlmacen;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.RequisicionAlmacenDetalleModel;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.RequisicionAlmacenModel;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.InsumoRequisicionDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionCreateDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionDetalleResponseDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionEstadoDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionPartidaRequestDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionResponseDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.out.persistence.repositories.RequisicionAlmacenRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequisicionAlmacenService {

    private static final Set<String> ROLES_RESOLUCION = Set.of(
            "ADMIN", "SUPER_ADMIN", "DIRECTOR_GENERAL", "SUBDIRECCION_ADMINISTRATIVA");
    private static final Set<String> ROLES_VISIBILIDAD_GLOBAL = ROLES_RESOLUCION;

    private final RequisicionAlmacenRepository requisicionRepository;
    private final InsumoRepository insumoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    @Transactional
    public RequisicionResponseDTO crear(RequisicionCreateDTO dto, String emailUsuario) {
        UsuarioModel solicitante = obtenerUsuario(emailUsuario);
        Set<Long> ids = new HashSet<>();
        RequisicionAlmacenModel requisicion = new RequisicionAlmacenModel();
        requisicion.setFolio(generarFolio());
        requisicion.setSolicitante(solicitante);
        requisicion.setSolicitanteNombre(nombreUsuario(solicitante));
        requisicion.setDestinatarioRol("SUBDIRECCION_ADMINISTRATIVA");
        requisicion.setEstado(EstadoRequisicionAlmacen.ENVIADA);
        requisicion.setObservaciones(limpiar(dto.getObservaciones()));

        for (RequisicionPartidaRequestDTO partida : dto.getPartidas()) {
            if (!ids.add(partida.getInsumoId())) {
                throw new ValidationException("No puedes agregar el mismo insumo más de una vez");
            }
            InsumoModel insumo = insumoRepository.findById(partida.getInsumoId())
                    .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                    .orElseThrow(() -> new BadRequestException("El insumo seleccionado no existe o está inactivo"));
            requisicion.agregarDetalle(crearDetalle(insumo, partida));
        }

        RequisicionAlmacenModel guardada = requisicionRepository.save(requisicion);
        notificacionService.notificarRoles(
                Set.of("SUBDIRECCION_ADMINISTRATIVA"),
                TipoNotificacion.ACCION_REQUERIDA,
                "Nueva requisición de almacén",
                guardada.getFolio() + " fue enviada por " + guardada.getSolicitanteNombre(),
                "ALMACEN",
                "REQUISICION_ALMACEN",
                guardada.getId(),
                "/almacen/requisiciones/" + guardada.getId());
        return map(guardada, true);
    }

    @Transactional(readOnly = true)
    public Page<RequisicionResponseDTO> listar(
            EstadoRequisicionAlmacen estado,
            String busqueda,
            Pageable pageable,
            String emailUsuario) {
        UsuarioModel usuario = obtenerUsuario(emailUsuario);
        Long solicitanteId = tieneRol(usuario, ROLES_VISIBILIDAD_GLOBAL) ? null : usuario.getId();
        String filtro = StringUtils.hasText(busqueda) ? busqueda.trim() : null;
        return requisicionRepository.buscar(solicitanteId, estado, filtro, pageable)
                .map(item -> map(item, false));
    }

    @Transactional(readOnly = true)
    public RequisicionResponseDTO obtener(Long id, String emailUsuario) {
        UsuarioModel usuario = obtenerUsuario(emailUsuario);
        RequisicionAlmacenModel requisicion = buscar(id);
        validarLectura(requisicion, usuario);
        return map(requisicion, true);
    }

    @Transactional(readOnly = true)
    public List<InsumoRequisicionDTO> sugerencias() {
        return insumoRepository.findWithStockBajo().stream()
                .filter(insumo -> insumo.getStockMinimo() != null
                        && valor(insumo.getStockActual()) < valor(insumo.getStockMinimo()))
                .map(this::mapInsumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InsumoRequisicionDTO> buscarInsumos(String busqueda) {
        String filtro = StringUtils.hasText(busqueda) ? busqueda.trim() : null;
        return insumoRepository.buscarPorTermino(filtro, true).stream()
                .limit(50)
                .map(this::mapInsumo)
                .toList();
    }

    @Transactional
    public RequisicionResponseDTO cambiarEstado(
            Long id,
            RequisicionEstadoDTO dto,
            String emailUsuario) {
        UsuarioModel usuario = obtenerUsuario(emailUsuario);
        RequisicionAlmacenModel requisicion = buscar(id);
        EstadoRequisicionAlmacen destino = dto.getEstado();

        if (destino == EstadoRequisicionAlmacen.CANCELADA) {
            if (!requisicion.getSolicitante().getId().equals(usuario.getId())
                    && !tieneRol(usuario, ROLES_RESOLUCION)) {
                throw new ValidationException("Sólo el solicitante puede cancelar esta requisición");
            }
            if (!Set.of(EstadoRequisicionAlmacen.ENVIADA, EstadoRequisicionAlmacen.EN_REVISION)
                    .contains(requisicion.getEstado())) {
                throw new ValidationException("La requisición ya no puede cancelarse");
            }
        } else {
            if (!tieneRol(usuario, ROLES_RESOLUCION)) {
                throw new ValidationException("Sólo Subdirección Administrativa puede resolver requisiciones");
            }
            validarTransicionAdministrativa(requisicion.getEstado(), destino);
        }

        requisicion.setEstado(destino);
        requisicion.setComentarioResolucion(limpiar(dto.getComentario()));
        requisicion.setResueltoPor(emailUsuario);
        if (Set.of(EstadoRequisicionAlmacen.AUTORIZADA, EstadoRequisicionAlmacen.RECHAZADA,
                EstadoRequisicionAlmacen.CANCELADA).contains(destino)) {
            requisicion.setFechaResolucion(LocalDateTime.now());
        }
        RequisicionAlmacenModel guardada = requisicionRepository.save(requisicion);
        notificarCambioEstado(guardada, destino);
        return map(guardada, true);
    }

    private void notificarCambioEstado(
            RequisicionAlmacenModel requisicion,
            EstadoRequisicionAlmacen estado) {
        String ruta = "/almacen/requisiciones/" + requisicion.getId();
        if (estado == EstadoRequisicionAlmacen.CANCELADA) {
            notificacionService.notificarRoles(
                    Set.of("SUBDIRECCION_ADMINISTRATIVA"),
                    TipoNotificacion.INFORMACION,
                    "Requisición cancelada",
                    requisicion.getFolio() + " fue cancelada por el solicitante",
                    "ALMACEN",
                    "REQUISICION_ALMACEN",
                    requisicion.getId(),
                    ruta);
            return;
        }

        TipoNotificacion tipo = switch (estado) {
            case AUTORIZADA -> TipoNotificacion.EXITO;
            case RECHAZADA -> TipoNotificacion.ALERTA;
            default -> TipoNotificacion.INFORMACION;
        };
        notificacionService.notificarUsuario(
                requisicion.getSolicitante(),
                tipo,
                "Requisición " + estado.getEtiqueta().toLowerCase(Locale.ROOT),
                requisicion.getFolio() + " cambió a " + estado.getEtiqueta(),
                "ALMACEN",
                "REQUISICION_ALMACEN",
                requisicion.getId(),
                ruta);
    }

    private void validarTransicionAdministrativa(
            EstadoRequisicionAlmacen actual,
            EstadoRequisicionAlmacen destino) {
        boolean valida = actual == EstadoRequisicionAlmacen.ENVIADA
                && Set.of(EstadoRequisicionAlmacen.EN_REVISION, EstadoRequisicionAlmacen.AUTORIZADA,
                        EstadoRequisicionAlmacen.RECHAZADA).contains(destino)
                || actual == EstadoRequisicionAlmacen.EN_REVISION
                && Set.of(EstadoRequisicionAlmacen.AUTORIZADA, EstadoRequisicionAlmacen.RECHAZADA).contains(destino);
        if (!valida) {
            throw new ValidationException("La transición de estado solicitada no es válida");
        }
    }

    private RequisicionAlmacenDetalleModel crearDetalle(
            InsumoModel insumo,
            RequisicionPartidaRequestDTO dto) {
        RequisicionAlmacenDetalleModel detalle = new RequisicionAlmacenDetalleModel();
        detalle.setInsumo(insumo);
        detalle.setInsumoCodigo(insumo.getCodigo());
        detalle.setInsumoNombre(insumo.getNombre());
        detalle.setUnidadSimbolo(insumo.getUnidadMedida() == null ? null : insumo.getUnidadMedida().getSimbolo());
        detalle.setCantidadSolicitada(dto.getCantidadSolicitada());
        detalle.setStockActualSnapshot(valor(insumo.getStockActual()));
        detalle.setStockMinimoSnapshot(insumo.getStockMinimo());
        detalle.setOrigenSugerencia(Boolean.TRUE.equals(dto.getOrigenSugerencia()));
        detalle.setObservaciones(limpiar(dto.getObservaciones()));
        return detalle;
    }

    private InsumoRequisicionDTO mapInsumo(InsumoModel insumo) {
        double actual = valor(insumo.getStockActual());
        double minimo = valor(insumo.getStockMinimo());
        double faltante = Math.max(minimo - actual, 0);
        return InsumoRequisicionDTO.builder()
                .id(insumo.getId())
                .codigo(insumo.getCodigo())
                .nombre(insumo.getNombre())
                .unidadSimbolo(insumo.getUnidadMedida() == null ? null : insumo.getUnidadMedida().getSimbolo())
                .stockActual(actual)
                .stockMinimo(insumo.getStockMinimo())
                .faltanteMinimo(faltante)
                .cantidadSugerida(faltante > 0 ? faltante : 1)
                .bajoMinimo(insumo.getStockMinimo() != null && actual < minimo)
                .build();
    }

    private RequisicionResponseDTO map(RequisicionAlmacenModel requisicion, boolean incluirPartidas) {
        List<RequisicionDetalleResponseDTO> partidas = incluirPartidas
                ? requisicion.getDetalles().stream().map(detalle -> RequisicionDetalleResponseDTO.builder()
                        .id(detalle.getId())
                        .insumoId(detalle.getInsumo().getId())
                        .insumoCodigo(detalle.getInsumoCodigo())
                        .insumoNombre(detalle.getInsumoNombre())
                        .unidadSimbolo(detalle.getUnidadSimbolo())
                        .cantidadSolicitada(detalle.getCantidadSolicitada())
                        .stockActualSnapshot(detalle.getStockActualSnapshot())
                        .stockMinimoSnapshot(detalle.getStockMinimoSnapshot())
                        .origenSugerencia(detalle.getOrigenSugerencia())
                        .observaciones(detalle.getObservaciones())
                        .build()).toList()
                : List.of();
        return RequisicionResponseDTO.builder()
                .id(requisicion.getId())
                .folio(requisicion.getFolio())
                .solicitanteUsuarioId(requisicion.getSolicitante().getId())
                .solicitanteNombre(requisicion.getSolicitanteNombre())
                .destinatario("Subdirección Administrativa")
                .estado(requisicion.getEstado())
                .estadoEtiqueta(requisicion.getEstado().getEtiqueta())
                .observaciones(requisicion.getObservaciones())
                .comentarioResolucion(requisicion.getComentarioResolucion())
                .resueltoPor(requisicion.getResueltoPor())
                .fechaEnvio(requisicion.getFechaEnvio())
                .fechaResolucion(requisicion.getFechaResolucion())
                .fechaActualizacion(requisicion.getFechaActualizacion())
                .totalPartidas(requisicion.getDetalles().size())
                .partidas(partidas)
                .build();
    }

    private RequisicionAlmacenModel buscar(Long id) {
        return requisicionRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Requisición no encontrada"));
    }

    private UsuarioModel obtenerUsuario(String email) {
        return usuarioRepository.findOneByEmail(email)
                .orElseThrow(() -> new BadRequestException("No se pudo identificar al usuario autenticado"));
    }

    private void validarLectura(RequisicionAlmacenModel requisicion, UsuarioModel usuario) {
        if (!requisicion.getSolicitante().getId().equals(usuario.getId())
                && !tieneRol(usuario, ROLES_VISIBILIDAD_GLOBAL)) {
            throw new ValidationException("No tienes acceso a esta requisición");
        }
    }

    private boolean tieneRol(UsuarioModel usuario, Set<String> roles) {
        return usuario.getRoles().stream().map(RolModel::getName).anyMatch(roles::contains);
    }

    private String nombreUsuario(UsuarioModel usuario) {
        EmpleadoModel empleado = usuario.getEmpleado();
        if (empleado == null) {
            return usuario.getEmail();
        }
        return String.join(" ",
                valorTexto(empleado.getNombre()),
                valorTexto(empleado.getApellidoPaterno()),
                valorTexto(empleado.getApellidoMaterno())).trim();
    }

    private String generarFolio() {
        return "REQ-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private double valor(Double numero) {
        return numero == null ? 0 : numero;
    }

    private String valorTexto(String texto) {
        return texto == null ? "" : texto;
    }

    private String limpiar(String texto) {
        return StringUtils.hasText(texto) ? texto.trim() : null;
    }
}
