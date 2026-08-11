package com.mobilesco.mobilesco_back.modules.auth.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.PermisoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.AccesoAuditLogRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.InvitacionUsuarioRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.PermisoRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.RolRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.out.persistence.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;

class AccesoServiceTest {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private PermisoRepository permisoRepository;
    private AccesoService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        permisoRepository = mock(PermisoRepository.class);
        service = new AccesoService(
                usuarioRepository,
                mock(InvitacionUsuarioRepository.class),
                mock(EmpleadoRepository.class),
                rolRepository,
                permisoRepository,
                mock(AccesoAuditLogRepository.class),
                mock(PasswordEncoder.class));
    }

    @Test
    void agregaLaVistaRequeridaAlAsignarUnaAccion() {
        PermisoModel accion = permiso("ACTION_USERS_CREATE", true);
        PermisoModel vista = permiso("VIEW_USERS", true);
        when(permisoRepository.findByCodeIn(anyCollection())).thenAnswer(invocation -> {
            Collection<String> solicitados = invocation.getArgument(0);
            return List.of(accion, vista).stream()
                    .filter(permiso -> solicitados.contains(permiso.getCode()))
                    .toList();
        });
        when(rolRepository.save(any(RolModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RolResponseDTO resultado = service.crearRol(rol("ROL_PRUEBA", "ACTION_USERS_CREATE"), "admin@test");

        assertEquals(List.of("ACTION_USERS_CREATE", "VIEW_USERS"), resultado.getPermisos());
    }

    @Test
    void rechazaUnPermisoInactivoAunqueExistaEnBaseDeDatos() {
        PermisoModel accionInactiva = permiso("ACTION_USERS_CREATE", false);
        PermisoModel vista = permiso("VIEW_USERS", true);
        when(permisoRepository.findByCodeIn(anyCollection())).thenReturn(List.of(accionInactiva, vista));

        BadRequestException error = assertThrows(
                BadRequestException.class,
                () -> service.crearRol(rol("ROL_PRUEBA", "ACTION_USERS_CREATE"), "admin@test"));

        assertTrue(error.getMessage().contains("ACTION_USERS_CREATE"));
        verify(rolRepository, never()).save(any(RolModel.class));
    }

    @Test
    void noExponePermisosInactivosEnRolesConfigurables() {
        RolModel rol = new RolModel();
        rol.setName("ROL_PRUEBA");
        rol.setPermisos(Set.of(permiso("VIEW_USERS", true), permiso("ACTION_USERS_CREATE", false)));
        when(rolRepository.findAll()).thenReturn(List.of(rol));

        List<RolResponseDTO> resultado = service.listarRolesDetalle();

        assertEquals(List.of("VIEW_USERS"), resultado.getFirst().getPermisos());
    }

    @Test
    void subdireccionAdministrativaConservaElCatalogoCompleto() {
        RolModel rol = new RolModel();
        rol.setName("SUBDIRECCION_ADMINISTRATIVA");
        rol.setPermisos(Set.of());
        when(rolRepository.findAll()).thenReturn(List.of(rol));

        List<RolResponseDTO> resultado = service.listarRolesDetalle();

        assertEquals(PermisoCatalog.ALL_CODES.stream().sorted().toList(), resultado.getFirst().getPermisos());
    }

    @Test
    void noExponePermisosDirectosInactivosDelUsuario() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail("usuario@test");
        usuario.setPermisos(Set.of(permiso("VIEW_USERS", true), permiso("ACTION_USERS_CREATE", false)));
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        var resultado = service.listarUsuarios();

        assertEquals(List.of("VIEW_USERS"), resultado.getFirst().getPermisosDirectos());
        assertEquals(List.of("VIEW_USERS"), resultado.getFirst().getPermisosEfectivos());
    }

    private RolCreateDTO rol(String nombre, String permiso) {
        RolCreateDTO dto = new RolCreateDTO();
        dto.setName(nombre);
        dto.setDescripcion("Rol para pruebas");
        dto.setPermisos(List.of(permiso));
        return dto;
    }

    private PermisoModel permiso(String code, boolean activo) {
        PermisoModel permiso = new PermisoModel();
        permiso.setCode(code);
        permiso.setActivo(activo);
        return permiso;
    }
}
