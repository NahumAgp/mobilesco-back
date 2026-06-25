package com.mobilesco.mobilesco_back.modules.empleado.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.RefreshTokenRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.in.api.dtos.EmpleadoCreateDTO;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.out.persistence.repositories.EmpleadoRepository;

class EmpleadoServiceTest {

    private EmpleadoRepository empleadoRepository;
    private UsuarioRepository usuarioRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private EmpleadoService service;

    @BeforeEach
    void setUp() {
        empleadoRepository = mock(EmpleadoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        service = new EmpleadoService(
                empleadoRepository,
                usuarioRepository,
                refreshTokenRepository,
                passwordEncoder
        );
    }

    @Test
    void creaEmpleadoConCuentaSinRolesAsignados() {
        EmpleadoCreateDTO dto = new EmpleadoCreateDTO();
        dto.setNombre("Ana");
        dto.setApellidoPaterno("Lopez");
        dto.setApellidoMaterno("Diaz");
        dto.setTelefono("7712345678");
        dto.setFechaNacimiento("2000-05-20");
        dto.setEmail("ANA@EMPRESA.COM");

        EmpleadoModel empleadoGuardado = new EmpleadoModel();
        empleadoGuardado.setId(5L);
        empleadoGuardado.setNombre(dto.getNombre());
        empleadoGuardado.setApellidoPaterno(dto.getApellidoPaterno());
        empleadoGuardado.setApellidoMaterno(dto.getApellidoMaterno());
        empleadoGuardado.setTelefono(dto.getTelefono());
        empleadoGuardado.setFechaNacimiento(LocalDate.parse(dto.getFechaNacimiento()));
        empleadoGuardado.setActivo(true);

        when(empleadoRepository.save(any(EmpleadoModel.class))).thenReturn(empleadoGuardado);
        when(usuarioRepository.findByEmail("ana@empresa.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        UsuarioModel usuarioConCuenta = new UsuarioModel();
        usuarioConCuenta.setEmail("ana@empresa.com");
        usuarioConCuenta.setEmpleado(empleadoGuardado);

        when(usuarioRepository.save(any(UsuarioModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findByEmpleado(any(EmpleadoModel.class))).thenReturn(Optional.of(usuarioConCuenta));

        var response = service.crear(dto);

        ArgumentCaptor<UsuarioModel> usuarioCaptor = ArgumentCaptor.forClass(UsuarioModel.class);
        org.mockito.Mockito.verify(usuarioRepository).save(usuarioCaptor.capture());

        UsuarioModel usuarioGuardado = usuarioCaptor.getValue();

        assertTrue(usuarioGuardado.getRoles().isEmpty(), "La cuenta debe guardarse sin roles");
        assertEquals("ana@empresa.com", usuarioGuardado.getEmail());
        assertEquals("hash", usuarioGuardado.getPasswordHash());
        assertFalse(usuarioGuardado.isEnabled());
        assertTrue(usuarioGuardado.isLocked());
        assertEquals(EstadoCuentaUsuario.PENDING, usuarioGuardado.getEstadoCuenta());
        assertEquals("ana@empresa.com", response.getCorreo());
        assertTrue(response.getTieneCuenta());
    }
}
