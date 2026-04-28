package com.mobilesco.mobilesco_back.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.Empleado.EmpleadoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Empleado.EmpleadoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Empleado.EmpleadoUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.models.RolModel;
import com.mobilesco.mobilesco_back.models.UsuarioModel;
import com.mobilesco.mobilesco_back.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.repositories.RolRepository;
import com.mobilesco.mobilesco_back.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository userRepository;
    private final RolRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(
            EmpleadoRepository empleadoRepository,
            UsuarioRepository userRepository,
            RolRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.empleadoRepository = empleadoRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
        empleado.setActivo(true);

        EmpleadoModel empleadoGuardado = empleadoRepository.save(empleado);

        boolean traeCuenta =
                dto.getEmail() != null && !dto.getEmail().isBlank()
                && dto.getPassword() != null && !dto.getPassword().isBlank();

        if (traeCuenta) {

            userRepository.findByEmail(dto.getEmail())
                .ifPresent(u -> { throw new BadRequestException("Ese correo ya está registrado."); });

            RolModel rolEmpleado = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new BadRequestException("No existe el rol EMPLOYEE"));

            UsuarioModel user = new UsuarioModel();
            user.setEmail(dto.getEmail());
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            user.setEnabled(true);
            user.setLocked(false);
            user.setEmpleado(empleadoGuardado);
            user.setRoles(Set.of(rolEmpleado));

            userRepository.save(user);
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

        existente.setNombre(dto.getNombre());
        existente.setApellidoPaterno(dto.getApellidoPaterno());
        existente.setApellidoMaterno(dto.getApellidoMaterno());
        existente.setTelefono(dto.getTelefono());

        if (dto.getFechaNacimiento() != null && !dto.getFechaNacimiento().isBlank()) {
            existente.setFechaNacimiento(LocalDate.parse(dto.getFechaNacimiento()));
        } else {
            existente.setFechaNacimiento(null);
        }

        EmpleadoModel guardado = empleadoRepository.save(existente);

        boolean traeCuenta =
                dto.getEmail() != null && !dto.getEmail().isBlank()
                && dto.getPassword() != null && !dto.getPassword().isBlank();

        UsuarioModel usuario = userRepository.findByEmpleado(guardado).orElse(null);

        if (usuario == null && traeCuenta) {

            userRepository.findByEmail(dto.getEmail())
                .ifPresent(u -> { throw new BadRequestException("Ese correo ya está registrado."); });

            RolModel rolEmpleado = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new BadRequestException("No existe el rol EMPLOYEE"));

            UsuarioModel nuevoUsuario = new UsuarioModel();
            nuevoUsuario.setEmail(dto.getEmail());
            nuevoUsuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            nuevoUsuario.setEnabled(true);
            nuevoUsuario.setLocked(false);
            nuevoUsuario.setEmpleado(guardado);
            nuevoUsuario.setRoles(Set.of(rolEmpleado));

            userRepository.save(nuevoUsuario);
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

    // =====================================================
    // 🔹 DELETE
    // =====================================================

 @Transactional
public void eliminar(Long id) {

    EmpleadoModel empleado = empleadoRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

    UsuarioModel usuario = userRepository.findByEmpleado(empleado).orElse(null);

    if (usuario != null) {

        // eliminar relación con roles
        usuario.getRoles().clear();

        userRepository.save(usuario);

        // eliminar usuario
        userRepository.delete(usuario);
    }

    // eliminar empleado
    empleadoRepository.delete(empleado);
}

    // =====================================================
    // 🔹 DESACTIVAR / ACTIVAR
    // =====================================================

    public EmpleadoResponseDTO cambiarActivo(Long id, Boolean activo) {

        EmpleadoModel existente = empleadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        existente.setActivo(activo);

        EmpleadoModel guardado = empleadoRepository.save(existente);

        return mapToResponseDTO(guardado);
    }
}
