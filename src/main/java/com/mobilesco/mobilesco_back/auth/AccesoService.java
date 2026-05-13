package com.mobilesco.mobilesco_back.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.auth.InvitacionUsuarioCreateDTO;
import com.mobilesco.mobilesco_back.dto.auth.InvitacionUsuarioResponseDTO;
import com.mobilesco.mobilesco_back.dto.auth.RegistroInvitacionRequestDTO;
import com.mobilesco.mobilesco_back.dto.auth.RegistroInvitacionResponseDTO;
import com.mobilesco.mobilesco_back.dto.auth.UsuarioPendienteResponseDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.EstadoCuentaUsuario;
import com.mobilesco.mobilesco_back.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.models.InvitacionUsuarioModel;
import com.mobilesco.mobilesco_back.models.RolModel;
import com.mobilesco.mobilesco_back.models.UsuarioModel;
import com.mobilesco.mobilesco_back.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.repositories.InvitacionUsuarioRepository;
import com.mobilesco.mobilesco_back.repositories.RolRepository;
import com.mobilesco.mobilesco_back.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class AccesoService {

    private final UsuarioRepository usuarioRepository;
    private final InvitacionUsuarioRepository invitacionUsuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AccesoService(
            UsuarioRepository usuarioRepository,
            InvitacionUsuarioRepository invitacionUsuarioRepository,
            EmpleadoRepository empleadoRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.invitacionUsuarioRepository = invitacionUsuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public InvitacionUsuarioResponseDTO crearInvitacion(InvitacionUsuarioCreateDTO dto, String creador) {
        String email = normalizarEmail(dto.getEmail());

        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ese correo ya tiene una cuenta registrada.");
        }

        if (invitacionUsuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ya existe una invitacion para ese correo.");
        }

        RolModel rol = rolRepository.findByName(dto.getRol())
                .orElseThrow(() -> new BadRequestException("El rol indicado no existe: " + dto.getRol()));

        InvitacionUsuarioModel invitacion = new InvitacionUsuarioModel();
        invitacion.setEmail(email);
        invitacion.setNombre(normalizarTexto(dto.getNombre()));
        invitacion.setApellidoPaterno(normalizarTexto(dto.getApellidoPaterno()));
        invitacion.setApellidoMaterno(normalizarTexto(dto.getApellidoMaterno()));
        invitacion.setTelefono(normalizarTexto(dto.getTelefono()));
        invitacion.setPuesto(dto.getPuesto().trim());
        invitacion.setRol(rol.getName());
        invitacion.setToken(generarToken());
        invitacion.setCreatedAt(LocalDateTime.now());
        invitacion.setCreatedBy(creador);
        invitacion.setUsed(false);

        InvitacionUsuarioModel guardada = invitacionUsuarioRepository.save(invitacion);
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

        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ese correo ya está registrado.");
        }

        RolModel rol = rolRepository.findByName(invitacion.getRol())
                .orElseThrow(() -> new BadRequestException("El rol indicado en la invitacion no existe."));

        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setEnabled(true);
        usuario.setLocked(false);
        usuario.setEstadoCuenta(EstadoCuentaUsuario.ACTIVE);
        usuario.setRoles(Set.of(rol));
        usuario.setInvitacion(invitacion);

        EmpleadoModel empleado = crearEmpleadoDesdeInvitacion(invitacion);
        empleadoRepository.save(empleado);
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

    public List<String> listarRolesDisponibles() {
        return rolRepository.findAll().stream()
                .map(RolModel::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional
    public List<UsuarioPendienteResponseDTO> listarPendientes() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> !esCuentaActiva(usuario))
                .map(this::mapPendiente)
                .toList();
    }

    @Transactional
    public UsuarioPendienteResponseDTO aprobarUsuario(Long userId, String emailAprobador) {
        UsuarioModel aprobador = usuarioRepository.findOneByEmail(emailAprobador)
                .orElseThrow(() -> new NotFoundException("Usuario aprobador no encontrado"));

        Set<String> roles = aprobador.getRoles().stream()
                .map(RolModel::getName)
                .collect(Collectors.toSet());

        boolean esDirector = roles.contains("DIRECTOR_GENERAL");
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
        return mapPendiente(usuario);
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

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim();
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
}
