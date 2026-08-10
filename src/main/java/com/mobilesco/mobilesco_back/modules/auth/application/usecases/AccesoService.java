package com.mobilesco.mobilesco_back.modules.auth.application.usecases;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.InvitacionUsuarioCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.InvitacionUsuarioResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.PermisoResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RegistroInvitacionRequestDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RegistroInvitacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolUpdateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioAccesoResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioAccesoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioPendienteResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.AccesoAuditLogModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario;
import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.InvitacionUsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.PermisoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.out.persistence.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.AccesoAuditLogRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.InvitacionUsuarioRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.PermisoRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.RolRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class AccesoService {

    private final UsuarioRepository usuarioRepository;
    private final InvitacionUsuarioRepository invitacionUsuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final AccesoAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public AccesoService(
            UsuarioRepository usuarioRepository,
            InvitacionUsuarioRepository invitacionUsuarioRepository,
            EmpleadoRepository empleadoRepository,
            RolRepository rolRepository,
            PermisoRepository permisoRepository,
            AccesoAuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.invitacionUsuarioRepository = invitacionUsuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public InvitacionUsuarioResponseDTO crearInvitacion(InvitacionUsuarioCreateDTO dto, String creador) {
        String email = normalizarEmail(dto.getEmail());

        RolModel rol = rolRepository.findByName(dto.getRol())
                .orElseThrow(() -> new BadRequestException("El rol indicado no existe: " + dto.getRol()));

        UsuarioModel usuarioExistente = usuarioRepository.findByEmail(email).orElse(null);
        EmpleadoModel empleadoInvitado = null;

        InvitacionUsuarioModel invitacion = new InvitacionUsuarioModel();
        invitacion.setEmail(email);
        invitacion.setNombre(normalizarTexto(dto.getNombre()));
        invitacion.setApellidoPaterno(normalizarTexto(dto.getApellidoPaterno()));
        invitacion.setApellidoMaterno(normalizarTextoOpcional(dto.getApellidoMaterno()));
        invitacion.setTelefono(normalizarTexto(dto.getTelefono()));
        invitacion.setPuesto(dto.getPuesto().trim());
        invitacion.setRol(rol.getName());
        invitacion.setEmpleadoId(dto.getEmpleadoId());
        invitacion.setToken(generarToken());
        invitacion.setCreatedAt(LocalDateTime.now());
        invitacion.setCreatedBy(creador);
        invitacion.setUsed(false);

        if (dto.getEmpleadoId() != null) {
            empleadoInvitado = empleadoRepository.findById(dto.getEmpleadoId())
                    .orElseThrow(() -> new BadRequestException("El empleado indicado no existe."));
            UsuarioModel usuarioDelEmpleado = usuarioRepository.findByEmpleado(empleadoInvitado).orElse(null);
            if (usuarioDelEmpleado != null && !esCuentaPendienteSinAcceso(usuarioDelEmpleado, empleadoInvitado, email)) {
                throw new BadRequestException("Ese empleado ya tiene una cuenta de acceso.");
            }
        }

        if (usuarioExistente != null && !esCuentaPendienteSinAcceso(usuarioExistente, empleadoInvitado, email)) {
            throw new BadRequestException("Ese correo ya tiene una cuenta registrada.");
        }

        var invitacionExistente = invitacionUsuarioRepository.findByEmail(email);
        if (invitacionExistente.isPresent()) {
            InvitacionUsuarioModel existente = invitacionExistente.get();
            if (existente.isUsed()) {
                throw new BadRequestException("Ya existe una invitacion utilizada para ese correo.");
            }

            existente.setNombre(invitacion.getNombre());
            existente.setApellidoPaterno(invitacion.getApellidoPaterno());
            existente.setApellidoMaterno(invitacion.getApellidoMaterno());
            existente.setTelefono(invitacion.getTelefono());
            existente.setPuesto(invitacion.getPuesto());
            existente.setRol(invitacion.getRol());
            existente.setEmpleadoId(invitacion.getEmpleadoId());
            existente.setCreatedBy(creador);
            InvitacionUsuarioModel actualizada = invitacionUsuarioRepository.save(existente);
            registrarAuditoria("REUSAR_INVITACION", "INVITACION", actualizada.getId(), creador, "Invitacion existente para " + email + " con rol " + rol.getName());
            return mapInvitation(actualizada);
        }

        InvitacionUsuarioModel guardada = invitacionUsuarioRepository.save(invitacion);
        registrarAuditoria("CREAR_INVITACION", "INVITACION", guardada.getId(), creador, "Invitacion para " + email + " con rol " + rol.getName());
        return mapInvitation(guardada);
    }

    @Transactional
    public RegistroInvitacionResponseDTO registrar(RegistroInvitacionRequestDTO dto) {
        String email = normalizarEmail(dto.getEmail());

        InvitacionUsuarioModel invitacion = invitacionUsuarioRepository.findByToken(dto.getToken())
                .orElseThrow(() -> new BadRequestException("La invitacion no existe o ya no es valida."));

        if (!invitacion.getEmail().equalsIgnoreCase(email)) {
            throw new BadRequestException("La invitacion no corresponde a ese correo.");
        }

        if (invitacion.isUsed()) {
            throw new BadRequestException("Esta invitacion ya fue utilizada.");
        }

        RolModel rol = rolRepository.findByName(invitacion.getRol())
                .orElseThrow(() -> new BadRequestException("El rol indicado en la invitacion no existe."));

        EmpleadoModel empleado = resolverEmpleadoParaInvitacion(invitacion);
        UsuarioModel usuario = usuarioRepository.findByEmail(email)
                .filter(u -> esCuentaPendienteSinAcceso(u, empleado, email))
                .orElseGet(UsuarioModel::new);

        if (usuario.getId() == null && usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ese correo ya esta registrado.");
        }

        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setEnabled(true);
        usuario.setLocked(false);
        usuario.setEstadoCuenta(EstadoCuentaUsuario.ACTIVE);
        usuario.setRoles(new HashSet<>(Set.of(rol)));
        usuario.setInvitacion(invitacion);

        usuario.setEmpleado(empleado);

        UsuarioModel guardado = usuarioRepository.save(usuario);

        invitacion.setUsed(true);
        invitacion.setUsedAt(LocalDateTime.now());
        invitacion.setUsedBy(email);
        invitacion.setUsuario(guardado);
        invitacionUsuarioRepository.save(invitacion);

        RegistroInvitacionResponseDTO response = new RegistroInvitacionResponseDTO();
        response.setIdUsuario(guardado.getId());
        response.setCorreo(guardado.getEmail());
        response.setMensaje("Registro creado y activado correctamente.");
        return response;
    }

    private boolean esCuentaPendienteSinAcceso(UsuarioModel usuario, EmpleadoModel empleado, String email) {
        if (usuario == null) {
            return false;
        }

        boolean mismoCorreo = email == null || usuario.getEmail().equalsIgnoreCase(email);
        boolean sinRoles = usuario.getRoles() == null || usuario.getRoles().isEmpty();
        boolean pendiente = usuario.getEstadoCuenta() == EstadoCuentaUsuario.PENDING;
        boolean bloqueada = !usuario.isEnabled() || usuario.isLocked();
        boolean mismoEmpleado = empleado == null
                || usuario.getEmpleado() == null
                || usuario.getEmpleado().getId().equals(empleado.getId());

        return mismoCorreo && sinRoles && pendiente && bloqueada && mismoEmpleado;
    }

    public List<String> listarRolesDisponibles() {
        return rolRepository.findAllNamesOrderByName();
    }

    public List<PermisoResponseDTO> listarPermisos() {
        return permisoRepository.findByActivoTrueOrderByModuloAscNombreAsc().stream()
                .map(this::mapPermiso)
                .toList();
    }

    public List<RolResponseDTO> listarRolesDetalle() {
        return rolRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(this::mapRol)
                .toList();
    }

    public PageResponseDTO<RolResponseDTO> listarRolesDetallePaginado(String busqueda, Pageable pageable) {
        Page<RolModel> page = rolRepository.buscarPaginado(normalizarFiltro(busqueda), pageable);
        List<Long> ids = page.getContent().stream().map(RolModel::getId).toList();
        Map<Long, RolModel> rolesConPermisos = ids.isEmpty()
                ? Map.of()
                : rolRepository.findAllWithPermisosByIdIn(ids).stream()
                        .collect(Collectors.toMap(RolModel::getId, Function.identity()));
        List<RolResponseDTO> content = page.getContent().stream()
                .map(rol -> rolesConPermisos.getOrDefault(rol.getId(), rol))
                .map(this::mapRol)
                .toList();

        return new PageResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    public RolResponseDTO crearRol(RolCreateDTO dto, String actor) {
        String nombreRol = normalizarRol(dto.getName());
        if (rolRepository.existsByName(nombreRol)) {
            throw new BadRequestException("Ya existe un rol con ese nombre.");
        }

        RolModel rol = new RolModel();
        rol.setName(nombreRol);
        rol.setDescripcion(normalizarTexto(dto.getDescripcion()));
        rol.setSistema(false);
        rol.setPermisos(resolvePermisos(dto.getPermisos()));

        RolModel guardado = rolRepository.save(rol);
        registrarAuditoria("CREAR_ROL", "ROL", guardado.getId(), actor, "Rol " + guardado.getName());
        return mapRol(guardado);
    }

    @Transactional
    public RolResponseDTO actualizarRol(Long id, RolUpdateDTO dto, String actor) {
        RolModel rol = rolRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado"));

        rol.setDescripcion(normalizarTexto(dto.getDescripcion()));
        if (esRolCompleto(rol.getName())) {
            rol.setPermisos(resolvePermisos(PermisoCatalog.ALL_CODES.stream().toList()));
        } else if (dto.getPermisos() != null) {
            Set<String> anteriores = rol.getPermisos().stream().map(PermisoModel::getCode).collect(Collectors.toSet());
            Set<PermisoModel> nuevos = resolvePermisos(dto.getPermisos());
            rol.setPermisos(nuevos);
            registrarCambioPermisos("ROL", rol.getId(), actor, anteriores,
                    nuevos.stream().map(PermisoModel::getCode).collect(Collectors.toSet()));
        }

        RolModel guardado = rolRepository.save(rol);
        registrarAuditoria("ACTUALIZAR_ROL", "ROL", guardado.getId(), actor, "Permisos actualizados para " + guardado.getName());
        return mapRol(guardado);
    }

    public List<UsuarioAccesoResponseDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .sorted((a, b) -> a.getEmail().compareToIgnoreCase(b.getEmail()))
                .map(this::mapUsuarioAcceso)
                .toList();
    }

    public PageResponseDTO<UsuarioAccesoResponseDTO> listarUsuariosPaginado(String busqueda, Pageable pageable) {
        Page<UsuarioAccesoResponseDTO> page = usuarioRepository.buscarPaginado(normalizarFiltro(busqueda), pageable)
                .map(this::mapUsuarioAcceso);

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    public UsuarioAccesoResponseDTO crearUsuario(UsuarioCreateDTO dto, String actor) {
        String email = normalizarEmail(dto.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ese correo ya tiene una cuenta registrada.");
        }

        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setEnabled(true);
        usuario.setLocked(false);
        usuario.setEstadoCuenta(EstadoCuentaUsuario.ACTIVE);
        usuario.setRoles(resolveRoles(dto.getRoles()));
        usuario.setPermisos(resolvePermisos(dto.getPermisosDirectos()));

        UsuarioModel guardado = usuarioRepository.save(usuario);
        registrarAuditoria("CREAR_USUARIO", "USUARIO", guardado.getId(), actor, "Usuario " + email);
        return mapUsuarioAcceso(guardado);
    }

    @Transactional
    public UsuarioAccesoResponseDTO actualizarUsuario(Long id, UsuarioAccesoUpdateDTO dto, String actor) {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        UsuarioModel usuarioActor = usuarioRepository.findOneByEmail(actor)
                .orElseThrow(() -> new NotFoundException("Usuario administrador no encontrado"));
        Set<String> rolesAnteriores = usuario.getRoles().stream().map(RolModel::getName).collect(Collectors.toSet());
        Set<String> permisosAnteriores = usuario.getPermisos().stream().map(PermisoModel::getCode).collect(Collectors.toSet());

        if (dto.getRoles() != null) {
            exigirPermiso(usuarioActor, "ACTION_USER_ROLES");
            validarNoEliminarUltimoAdmin(usuario, dto.getRoles(), null);
            usuario.setRoles(resolveRoles(dto.getRoles()));
        }
        if (dto.getPermisosDirectos() != null) {
            exigirPermiso(usuarioActor, "ACTION_USER_PERMISSIONS");
            usuario.setPermisos(resolvePermisos(dto.getPermisosDirectos()));
        }
        if (dto.getEnabled() != null) {
            exigirPermiso(usuarioActor, "ACTION_USERS_STATUS");
            validarNoEliminarUltimoAdmin(usuario, null, dto.getEnabled());
            usuario.setEnabled(dto.getEnabled());
            usuario.setEstadoCuenta(dto.getEnabled() ? EstadoCuentaUsuario.ACTIVE : EstadoCuentaUsuario.SUSPENDED);
        }
        if (dto.getLocked() != null) {
            exigirPermiso(usuarioActor, "ACTION_USERS_STATUS");
            if (dto.getLocked()) {
                validarNoEliminarUltimoAdmin(usuario, null, false);
            }
            usuario.setLocked(dto.getLocked());
        }

        UsuarioModel guardado = usuarioRepository.save(usuario);
        Set<String> rolesNuevos = guardado.getRoles().stream().map(RolModel::getName).collect(Collectors.toSet());
        Set<String> permisosNuevos = guardado.getPermisos().stream().map(PermisoModel::getCode).collect(Collectors.toSet());
        registrarAuditoria("ACTUALIZAR_USUARIO", "USUARIO", guardado.getId(), actor,
                "Usuario " + guardado.getEmail() + "; " + describirCambios("roles", rolesAnteriores, rolesNuevos)
                        + "; " + describirCambios("permisos directos", permisosAnteriores, permisosNuevos));
        return mapUsuarioAcceso(guardado);
    }

    @Transactional
    public UsuarioAccesoResponseDTO desactivarUsuario(Long id, String actor) {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        validarNoEliminarUltimoAdmin(usuario, null, false);
        usuario.setEnabled(false);
        usuario.setLocked(true);
        usuario.setEstadoCuenta(EstadoCuentaUsuario.SUSPENDED);

        UsuarioModel guardado = usuarioRepository.save(usuario);
        registrarAuditoria("DESACTIVAR_USUARIO", "USUARIO", guardado.getId(), actor, "Usuario " + guardado.getEmail());
        return mapUsuarioAcceso(guardado);
    }

    @Transactional
    public List<UsuarioPendienteResponseDTO> listarPendientes() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> !esCuentaActiva(usuario))
                .map(this::mapPendiente)
                .toList();
    }

    @Transactional
    public PageResponseDTO<UsuarioPendienteResponseDTO> listarPendientesPaginado(Pageable pageable) {
        Page<UsuarioPendienteResponseDTO> page = usuarioRepository.buscarPendientes(EstadoCuentaUsuario.ACTIVE, pageable)
                .map(this::mapPendiente);

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    public UsuarioPendienteResponseDTO aprobarUsuario(Long userId, String emailAprobador) {
        UsuarioModel aprobador = usuarioRepository.findOneByEmail(emailAprobador)
                .orElseThrow(() -> new NotFoundException("Usuario aprobador no encontrado"));

        Set<String> roles = aprobador.getRoles().stream()
                .map(RolModel::getName)
                .collect(Collectors.toSet());

        boolean esDirector = roles.contains("DIRECTOR_GENERAL") || roles.contains("SUBDIRECCION_ADMINISTRATIVA");
        boolean esDev = roles.contains("ADMIN") || roles.contains("SUPER_ADMIN");

        if (!esDirector && !esDev) {
            throw new BadRequestException("No tienes permisos para aprobar usuarios.");
        }

        UsuarioModel usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (esCuentaActiva(usuario)) {
            return mapPendiente(usuario);
        }

        if (esDirector) {
            String aprobadorDirector = obtenerNombreCorto(aprobador);
            String aprobadorDirectorEmail = aprobador.getEmail();
            if (usuario.getApprovedByDirector() == null) {
                if (aprobadorDirectorEmail.equalsIgnoreCase(usuario.getApprovedByDevEmail())) {
                    throw new BadRequestException("La aprobacion debe venir de una persona distinta a la del Dev.");
                }
                usuario.setApprovedByDirector(aprobadorDirector);
                usuario.setApprovedByDirectorEmail(aprobadorDirectorEmail);
                usuario.setApprovedByDirectorAt(LocalDateTime.now());
            }
        }

        if (esDev) {
            String aprobadorDev = obtenerNombreCorto(aprobador);
            String aprobadorDevEmail = aprobador.getEmail();
            if (usuario.getApprovedByDev() == null) {
                if (aprobadorDevEmail.equalsIgnoreCase(usuario.getApprovedByDirectorEmail())) {
                    throw new BadRequestException("La aprobacion debe venir de una persona distinta a la de Direccion General.");
                }
                usuario.setApprovedByDev(aprobadorDev);
                usuario.setApprovedByDevEmail(aprobadorDevEmail);
                usuario.setApprovedByDevAt(LocalDateTime.now());
            }
        }

        if (usuario.getApprovedByDirector() != null && usuario.getApprovedByDev() != null) {
            usuario.setEstadoCuenta(EstadoCuentaUsuario.ACTIVE);
            usuario.setEnabled(true);
            usuario.setLocked(false);
        }

        usuarioRepository.save(usuario);
        registrarAuditoria("APROBAR_USUARIO", "USUARIO", usuario.getId(), emailAprobador, "Aprobacion de usuario " + usuario.getEmail());
        return mapPendiente(usuario);
    }

    public Set<String> obtenerPermisosEfectivos(UsuarioModel usuario) {
        Set<String> permisos = new HashSet<>();
        usuario.getRoles().forEach(rol -> rol.getPermisos().stream()
                .filter(PermisoModel::isActivo)
                .map(PermisoModel::getCode)
                .forEach(permisos::add));
        usuario.getPermisos().stream()
                .filter(PermisoModel::isActivo)
                .map(PermisoModel::getCode)
                .forEach(permisos::add);

        if (usuario.getRoles().stream().anyMatch(rol -> esRolCompleto(rol.getName()))) {
            permisos.addAll(PermisoCatalog.ALL_CODES);
        }
        permisos.removeIf(codigo -> {
            String vistaRequerida = PermisoCatalog.requiredView(codigo);
            return vistaRequerida != null && !permisos.contains(vistaRequerida);
        });
        return permisos;
    }

    public boolean usuarioTienePermiso(UsuarioModel usuario, String permiso) {
        return obtenerPermisosEfectivos(usuario).contains(permiso);
    }

    private InvitacionUsuarioResponseDTO mapInvitation(InvitacionUsuarioModel invitacion) {
        InvitacionUsuarioResponseDTO dto = new InvitacionUsuarioResponseDTO();
        dto.setId(invitacion.getId());
        dto.setEmail(invitacion.getEmail());
        dto.setNombre(invitacion.getNombre());
        dto.setApellidoPaterno(invitacion.getApellidoPaterno());
        dto.setApellidoMaterno(invitacion.getApellidoMaterno());
        dto.setTelefono(invitacion.getTelefono());
        dto.setPuesto(invitacion.getPuesto());
        dto.setRol(invitacion.getRol());
        dto.setToken(invitacion.getToken());
        dto.setUsed(invitacion.isUsed());
        dto.setCreatedAt(invitacion.getCreatedAt());
        dto.setUsedAt(invitacion.getUsedAt());
        dto.setCreatedBy(invitacion.getCreatedBy());
        dto.setUsedBy(invitacion.getUsedBy());
        dto.setEmpleadoId(invitacion.getEmpleadoId());
        return dto;
    }

    private UsuarioPendienteResponseDTO mapPendiente(UsuarioModel usuario) {
        UsuarioPendienteResponseDTO dto = new UsuarioPendienteResponseDTO();
        dto.setIdUsuario(usuario.getId());
        dto.setCorreo(usuario.getEmail());
        dto.setEstadoCuenta(usuario.getEstadoCuenta().name());
        dto.setAprobacionDirector(usuario.getApprovedByDirector());
        dto.setAprobacionDev(usuario.getApprovedByDev());
        dto.setAprobacionDirectorAt(usuario.getApprovedByDirectorAt());
        dto.setAprobacionDevAt(usuario.getApprovedByDevAt());

        if (usuario.getInvitacion() != null) {
            dto.setPuesto(usuario.getInvitacion().getPuesto());
            dto.setRol(usuario.getInvitacion().getRol());
        } else {
            dto.setRol(usuario.getRoles().stream().findFirst().map(RolModel::getName).orElse(""));
        }

        dto.setFechaRegistro(
                usuario.getInvitacion() != null
                        ? usuario.getInvitacion().getUsedAt()
                        : usuario.getLastLoginAt()
        );
        return dto;
    }

    private PermisoResponseDTO mapPermiso(PermisoModel permiso) {
        PermisoResponseDTO dto = new PermisoResponseDTO();
        dto.setId(permiso.getId());
        dto.setCode(permiso.getCode());
        dto.setNombre(permiso.getNombre());
        dto.setModulo(permiso.getModulo());
        dto.setVista(permiso.getVista());
        dto.setDescripcion(permiso.getDescripcion());
        dto.setRuta(permiso.getRuta());
        dto.setTipo(permiso.getTipo());
        dto.setVistaRequerida(PermisoCatalog.requiredView(permiso.getCode()));
        dto.setActivo(permiso.isActivo());
        return dto;
    }

    private RolResponseDTO mapRol(RolModel rol) {
        RolResponseDTO dto = new RolResponseDTO();
        dto.setId(rol.getId());
        dto.setName(rol.getName());
        dto.setDescripcion(rol.getDescripcion());
        dto.setSistema(rol.isSistema());
        dto.setPermisos((esRolCompleto(rol.getName())
                ? PermisoCatalog.ALL_CODES.stream()
                : rol.getPermisos().stream().map(PermisoModel::getCode))
                .sorted().toList());
        return dto;
    }

    private UsuarioAccesoResponseDTO mapUsuarioAcceso(UsuarioModel usuario) {
        UsuarioAccesoResponseDTO dto = new UsuarioAccesoResponseDTO();
        dto.setIdUsuario(usuario.getId());
        dto.setCorreo(usuario.getEmail());
        dto.setEstadoCuenta(usuario.getEstadoCuenta() == null ? "" : usuario.getEstadoCuenta().name());
        dto.setEnabled(usuario.isEnabled());
        dto.setLocked(usuario.isLocked());
        if (usuario.getInvitacion() != null) {
            dto.setAccessGrantedAt(usuario.getInvitacion().getUsedAt());
        }
        dto.setLastLoginAt(usuario.getLastLoginAt());
        dto.setRoles(usuario.getRoles().stream().map(RolModel::getName).sorted().toList());
        dto.setPermisosDirectos(usuario.getPermisos().stream().map(PermisoModel::getCode).sorted().toList());
        dto.setPermisosHeredados(obtenerPermisosHeredados(usuario).stream().sorted().toList());
        dto.setPermisosEfectivos(obtenerPermisosEfectivos(usuario).stream().sorted().toList());

        if (usuario.getEmpleado() != null) {
            dto.setNombre(usuario.getEmpleado().getNombre());
            dto.setApellidoPaterno(usuario.getEmpleado().getApellidoPaterno());
            dto.setApellidoMaterno(usuario.getEmpleado().getApellidoMaterno());
        }
        return dto;
    }

    private Set<RolModel> resolveRoles(List<String> nombresRoles) {
        List<String> rolesSolicitados = nombresRoles == null ? List.of("EMPLOYEE") : nombresRoles;
        Set<String> normalizados = rolesSolicitados.stream()
                .map(this::normalizarRol)
                .collect(Collectors.toSet());
        if (normalizados.isEmpty()) {
            throw new BadRequestException("El usuario debe tener al menos un rol.");
        }
        Set<RolModel> roles = new HashSet<>(rolRepository.findByNameIn(normalizados));
        Set<String> encontrados = roles.stream().map(RolModel::getName).collect(Collectors.toSet());
        normalizados.removeAll(encontrados);
        if (!normalizados.isEmpty()) {
            throw new BadRequestException("Roles inexistentes: " + String.join(", ", normalizados));
        }

        return roles;
    }

    private Set<PermisoModel> resolvePermisos(List<String> codigos) {
        if (codigos == null || codigos.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalized = codigos.stream()
                .map(this::normalizarPermiso)
                .collect(Collectors.toSet());
        List<PermisoModel> permisos = permisoRepository.findByCodeIn(normalized);
        Set<String> encontrados = permisos.stream().map(PermisoModel::getCode).collect(Collectors.toSet());
        normalized.removeAll(encontrados);
        if (!normalized.isEmpty()) {
            throw new BadRequestException("Permisos inexistentes: " + String.join(", ", normalized));
        }
        return new HashSet<>(permisos);
    }

    private Set<String> obtenerPermisosHeredados(UsuarioModel usuario) {
        Set<String> permisos = usuario.getRoles().stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .filter(PermisoModel::isActivo)
                .map(PermisoModel::getCode)
                .collect(Collectors.toSet());
        if (usuario.getRoles().stream().anyMatch(rol -> esRolCompleto(rol.getName()))) {
            permisos.addAll(PermisoCatalog.ALL_CODES);
        }
        return permisos;
    }

    private boolean esRolCompleto(String nombre) {
        return "ADMIN".equals(nombre) || "DIRECTOR_GENERAL".equals(nombre);
    }

    private void exigirPermiso(UsuarioModel actor, String permiso) {
        if (!usuarioTienePermiso(actor, permiso)) {
            throw new BadRequestException("No tienes el permiso requerido: " + permiso);
        }
    }

    private void validarNoEliminarUltimoAdmin(UsuarioModel usuario, List<String> rolesSolicitados, Boolean habilitado) {
        boolean esAdmin = usuario.getRoles().stream().anyMatch(rol -> "ADMIN".equals(rol.getName()));
        if (!esAdmin || usuarioRepository.countAdministradoresActivos() > 1) {
            return;
        }
        boolean conservaRol = rolesSolicitados == null || rolesSolicitados.stream()
                .map(this::normalizarRol)
                .anyMatch("ADMIN"::equals);
        boolean conservaActivo = habilitado == null || habilitado;
        if (!conservaRol || !conservaActivo) {
            throw new BadRequestException("No se puede quitar o desactivar al último ADMIN activo.");
        }
    }

    private void registrarCambioPermisos(String entidad, Long entidadId, String actor, Set<String> anteriores, Set<String> nuevos) {
        registrarAuditoria("CAMBIAR_PERMISOS", entidad, entidadId, actor, describirCambios("permisos", anteriores, nuevos));
    }

    private String describirCambios(String etiqueta, Set<String> anteriores, Set<String> nuevos) {
        Set<String> agregados = new HashSet<>(nuevos);
        agregados.removeAll(anteriores);
        Set<String> retirados = new HashSet<>(anteriores);
        retirados.removeAll(nuevos);
        return etiqueta + " agregados=" + agregados.stream().sorted().toList()
                + ", retirados=" + retirados.stream().sorted().toList();
    }

    private String generarToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        } while (invitacionUsuarioRepository.existsByToken(token));
        return token;
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarRol(String rol) {
        return rol.trim().toUpperCase(Locale.ROOT).replace(" ", "_");
    }

    private String normalizarPermiso(String permiso) {
        return permiso.trim().toUpperCase(Locale.ROOT).replace(" ", "_");
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String normalizarFiltro(String texto) {
        String limpio = normalizarTexto(texto);
        return limpio.isBlank() ? null : limpio;
    }

    private String normalizarTextoOpcional(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private EmpleadoModel crearEmpleadoDesdeInvitacion(InvitacionUsuarioModel invitacion) {
        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setNombre(invitacion.getNombre());
        empleado.setApellidoPaterno(invitacion.getApellidoPaterno());
        empleado.setApellidoMaterno(invitacion.getApellidoMaterno());
        empleado.setTelefono(invitacion.getTelefono());
        empleado.setActivo(true);
        return empleado;
    }

    private EmpleadoModel resolverEmpleadoParaInvitacion(InvitacionUsuarioModel invitacion) {
        if (invitacion.getEmpleadoId() != null) {
            EmpleadoModel existente = empleadoRepository.findById(invitacion.getEmpleadoId())
                    .orElseThrow(() -> new BadRequestException("El empleado asociado a la invitacion ya no existe."));
            usuarioRepository.findByEmpleado(existente)
                    .filter(u -> !esCuentaPendienteSinAcceso(u, existente, invitacion.getEmail()))
                    .ifPresent(u -> {
                        throw new BadRequestException("El empleado asociado a la invitacion ya tiene una cuenta.");
                    });
            return existente;
        }

        EmpleadoModel empleado = crearEmpleadoDesdeInvitacion(invitacion);
        return empleadoRepository.save(empleado);
    }

    private String obtenerNombreCorto(UsuarioModel usuario) {
        if (usuario.getEmpleado() != null) {
            String nombre = usuario.getEmpleado().getNombre() == null ? "" : usuario.getEmpleado().getNombre().trim();
            String apellidoPaterno = usuario.getEmpleado().getApellidoPaterno() == null
                    ? ""
                    : usuario.getEmpleado().getApellidoPaterno().trim();
            String completo = (nombre + " " + apellidoPaterno).trim();
            if (!completo.isBlank()) {
                return completo;
            }
        }
        return usuario.getEmail();
    }

    private boolean esCuentaActiva(UsuarioModel usuario) {
        if (usuario.getEstadoCuenta() == EstadoCuentaUsuario.ACTIVE) {
            return true;
        }
        if (usuario.getEstadoCuenta() == null) {
            return usuario.isEnabled() && !usuario.isLocked();
        }
        return false;
    }

    private void registrarAuditoria(String accion, String entidad, Long entidadId, String actor, String detalle) {
        AccesoAuditLogModel log = new AccesoAuditLogModel();
        log.setAccion(accion);
        log.setEntidad(entidad);
        log.setEntidadId(entidadId);
        log.setActorEmail(actor == null ? "sistema" : actor);
        log.setDetalle(detalle);
        auditLogRepository.save(log);
    }
}
