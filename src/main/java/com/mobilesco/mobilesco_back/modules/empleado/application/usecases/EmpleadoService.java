package com.mobilesco.mobilesco_back.modules.empleado.application.usecases;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.in.api.dtos.EmpleadoCreateDTO;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.in.api.dtos.EmpleadoResponseDTO;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.in.api.dtos.EmpleadoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.areatrabajo.domain.models.AreaTrabajoModel;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.out.persistence.repositories.AreaTrabajoRepository;
import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.RefreshTokenModel;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.out.persistence.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.RefreshTokenRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class EmpleadoService {

    private static final Set<String> ROLES_GESTION_EMPLEADOS = Set.of(
            "ADMIN",
            "DIRECTOR_GENERAL",
            "SUBDIRECCION_ADMINISTRATIVA"
    );

    private final EmpleadoRepository empleadoRepository;
    private final AreaTrabajoRepository areaTrabajoRepository;
    private final UsuarioRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(
            EmpleadoRepository empleadoRepository,
            AreaTrabajoRepository areaTrabajoRepository,
            UsuarioRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.empleadoRepository = empleadoRepository;
        this.areaTrabajoRepository = areaTrabajoRepository;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // 🔹 MAPPER
    // =====================================================

    private EmpleadoResponseDTO mapToResponseDTO(EmpleadoModel empleado) {

        EmpleadoResponseDTO dto = new EmpleadoResponseDTO();

        dto.setId(empleado.getId());
        dto.setNombre(empleado.getNombre());
        dto.setApellidoPaterno(empleado.getApellidoPaterno());
        dto.setApellidoMaterno(empleado.getApellidoMaterno());
        dto.setTelefono(empleado.getTelefono());
        dto.setFechaNacimiento(empleado.getFechaNacimiento());
        dto.setFotoUrl(empleado.getFotoUrl());
        if (empleado.getAreaTrabajo() != null) {
            dto.setAreaId(empleado.getAreaTrabajo().getId());
            dto.setAreaNombre(empleado.getAreaTrabajo().getNombre());
        }
        dto.setActivo(empleado.getActivo());
        dto.setFechaRegistro(empleado.getFechaRegistro());

        // 🔹 Buscar usuario ligado
        UsuarioModel usuario = userRepository.findByEmpleado(empleado).orElse(null);

        if (usuario != null) {
            dto.setCorreo(usuario.getEmail());
            dto.setTieneCuenta(true);
        } else {
            dto.setTieneCuenta(false);
        }

        return dto;
    }

    private List<EmpleadoResponseDTO> mapToResponseDTOList(List<EmpleadoModel> empleados) {
        return empleados.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 🔹 CREATE
    // =====================================================

    @Transactional
    public EmpleadoResponseDTO crear(EmpleadoCreateDTO dto) {

        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setNombre(dto.getNombre());
        empleado.setApellidoPaterno(dto.getApellidoPaterno());
        empleado.setApellidoMaterno(dto.getApellidoMaterno());
        empleado.setTelefono(dto.getTelefono());
        if (dto.getFechaNacimiento() != null && !dto.getFechaNacimiento().isBlank()) {
            empleado.setFechaNacimiento(LocalDate.parse(dto.getFechaNacimiento()));
        }
        empleado.setAreaTrabajo(resolverArea(dto.getAreaId()));
        empleado.setActivo(true);

        EmpleadoModel empleadoGuardado = empleadoRepository.save(empleado);

        boolean traeCuenta = dto.getEmail() != null && !dto.getEmail().isBlank();

        if (traeCuenta) {
            crearCuentaEmpleadoSinRoles(empleadoGuardado, dto.getEmail());
        }

        return mapToResponseDTO(empleadoGuardado);
    }

    // =====================================================
    // 🔹 READ - Todos
    // =====================================================

    public List<EmpleadoResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(empleadoRepository.findAll());
    }

    // =====================================================
    // 🔹 READ - Por ID
    // =====================================================

    public EmpleadoResponseDTO obtenerPorId(Long id) {
        EmpleadoModel empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        return mapToResponseDTO(empleado);
    }

    // =====================================================
    // 🔹 READ - Por activo
    // =====================================================

    public List<EmpleadoResponseDTO> buscarPorActivo(Boolean activo) {
        return mapToResponseDTOList(empleadoRepository.findByActivo(activo));
    }

    // =====================================================
    // 🔹 READ - Por nombre
    // =====================================================

    public List<EmpleadoResponseDTO> buscarPorNombre(String nombre) {
        return mapToResponseDTOList(empleadoRepository.findByNombreContainingIgnoreCase(nombre));
    }

    // =====================================================
    // 🔹 READ - Por activo y nombre
    // =====================================================

    public List<EmpleadoResponseDTO> buscarPorActivoYNombre(Boolean activo, String nombre) {
        return mapToResponseDTOList(
                empleadoRepository.findByActivoAndNombreContainingIgnoreCase(activo, nombre));
    }

    // =====================================================
    // 🔹 READ - Listado con filtros opcionales
    // =====================================================

    public List<EmpleadoResponseDTO> listar(Boolean activo, String nombre) {
        boolean tieneNombre = nombre != null && !nombre.isBlank();

        if (activo != null && tieneNombre) {
            return buscarPorActivoYNombre(activo, nombre);
        }

        if (activo != null) {
            return buscarPorActivo(activo);
        }

        if (tieneNombre) {
            return buscarPorNombre(nombre);
        }

        return obtenerTodos();
    }

    // =====================================================
    // 🔹 UPDATE
    // =====================================================

    @Transactional
    public EmpleadoResponseDTO actualizar(Long id, EmpleadoUpdateDTO dto) {

        EmpleadoModel existente = empleadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        UsuarioModel usuarioActual = obtenerUsuarioActual();
        if (!puedeEditarEmpleado(usuarioActual, existente)) {
            throw new BadRequestException("No tienes permisos para editar este empleado.");
        }

        existente.setNombre(dto.getNombre());
        existente.setApellidoPaterno(dto.getApellidoPaterno());
        existente.setApellidoMaterno(dto.getApellidoMaterno());
        existente.setTelefono(dto.getTelefono());

        if (dto.getFechaNacimiento() != null && !dto.getFechaNacimiento().isBlank()) {
            existente.setFechaNacimiento(LocalDate.parse(dto.getFechaNacimiento()));
        } else {
            existente.setFechaNacimiento(null);
        }
        existente.setAreaTrabajo(resolverArea(dto.getAreaId()));

        if (dto.getActivo() != null && !dto.getActivo().equals(existente.getActivo())) {
            if (!puedeGestionarEmpleados(usuarioActual)) {
                throw new BadRequestException("No tienes permisos para cambiar el estado del empleado.");
            }

            existente.setActivo(dto.getActivo());
        }

        EmpleadoModel guardado = empleadoRepository.save(existente);

        boolean traeCuenta = dto.getEmail() != null && !dto.getEmail().isBlank();

        UsuarioModel usuario = userRepository.findByEmpleado(guardado).orElse(null);

        if (usuario == null && traeCuenta) {
            crearCuentaEmpleadoSinRoles(guardado, dto.getEmail());
        }

        if (usuario != null) {

            // actualizar email solo si viene
            if (dto.getEmail() != null && !dto.getEmail().isBlank()) {

                userRepository.findByEmail(dto.getEmail())
                        .filter(u -> !u.getId().equals(usuario.getId()))
                        .ifPresent(u -> { throw new BadRequestException("Ese correo ya está registrado."); });

                usuario.setEmail(dto.getEmail());
            }

            // actualizar password solo si viene
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            }

            userRepository.save(usuario);
        }

        return mapToResponseDTO(guardado);
    }

    private UsuarioModel crearCuentaEmpleadoSinRoles(EmpleadoModel empleado, String email) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmail(emailNormalizado)
                .ifPresent(u -> { throw new BadRequestException("Ese correo ya está registrado."); });

        UsuarioModel user = new UsuarioModel();
        user.setEmail(emailNormalizado);
        user.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        user.setEnabled(false);
        user.setLocked(true);
        user.setEstadoCuenta(EstadoCuentaUsuario.PENDING);
        user.setEmpleado(empleado);
        user.setRoles(new HashSet<>());

        return userRepository.save(user);
    }

    private AreaTrabajoModel resolverArea(Long areaId) {
        if (areaId == null) {
            return null;
        }

        return areaTrabajoRepository.findById(areaId)
                .orElseThrow(() -> new BadRequestException("El area indicada no existe."));
    }

    private UsuarioModel obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("No se pudo identificar al usuario autenticado.");
        }

        return userRepository.findOneByEmail(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Usuario autenticado no encontrado."));
    }

    private boolean puedeEditarEmpleado(UsuarioModel usuarioActual, EmpleadoModel empleadoObjetivo) {
        if (usuarioActual == null) {
            return false;
        }

        if (puedeGestionarEmpleados(usuarioActual)) {
            return true;
        }

        EmpleadoModel empleadoPropio = usuarioActual.getEmpleado();
        return empleadoPropio != null
                && empleadoPropio.getId() != null
                && empleadoPropio.getId().equals(empleadoObjetivo.getId());
    }

    private boolean puedeGestionarEmpleados(UsuarioModel usuarioActual) {
        if (usuarioActual == null || usuarioActual.getRoles() == null) {
            return false;
        }

        return usuarioActual.getRoles().stream()
                .map(RolModel::getName)
                .anyMatch(ROLES_GESTION_EMPLEADOS::contains);
    }

    // =====================================================
    // 🔹 DELETE
    // =====================================================

    @Transactional
    public void eliminar(Long id) {

        EmpleadoModel empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        UsuarioModel usuarioActual = obtenerUsuarioActual();
        if (!puedeGestionarEmpleados(usuarioActual)) {
            throw new BadRequestException("No tienes permisos para eliminar empleados.");
        }

        EmpleadoModel empleadoPropio = usuarioActual.getEmpleado();
        if (empleadoPropio != null && empleadoPropio.getId() != null && empleadoPropio.getId().equals(id)) {
            throw new BadRequestException("No puedes eliminar tu propio empleado. Desactivalo si necesitas bloquearlo.");
        }

        UsuarioModel usuario = userRepository.findByEmpleado(empleado).orElse(null);

        if (usuario != null) {
            eliminarRelacionesAutenticacion(usuario);

            usuario.getRoles().clear();

            userRepository.save(usuario);
            userRepository.flush();

            userRepository.delete(usuario);
        }

        empleadoRepository.delete(empleado);
    }

    private void eliminarRelacionesAutenticacion(UsuarioModel usuario) {
        List<RefreshTokenModel> tokens = refreshTokenRepository.findByUser(usuario);

        if (tokens.isEmpty()) {
            return;
        }

        tokens.forEach(token -> token.setReplacedBy(null));
        refreshTokenRepository.saveAll(tokens);
        refreshTokenRepository.deleteAll(tokens);
        refreshTokenRepository.flush();
    }

    // =====================================================
    // 🔹 DESACTIVAR / ACTIVAR
    // =====================================================

    @Transactional
    public EmpleadoResponseDTO cambiarActivo(Long id, Boolean activo) {

        EmpleadoModel existente = empleadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        UsuarioModel usuarioActual = obtenerUsuarioActual();
        if (!puedeGestionarEmpleados(usuarioActual)) {
            throw new BadRequestException("No tienes permisos para cambiar el estado del empleado.");
        }

        existente.setActivo(activo);

        EmpleadoModel guardado = empleadoRepository.save(existente);

        return mapToResponseDTO(guardado);
    }
}
