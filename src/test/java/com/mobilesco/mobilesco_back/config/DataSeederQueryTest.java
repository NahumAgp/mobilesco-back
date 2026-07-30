package com.mobilesco.mobilesco_back.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.PermisoCatalog;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.PermisoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.PermisoRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.RolRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.out.persistence.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.out.persistence.repositories.TipoInsumoRepository;

class DataSeederQueryTest {

    @Test
    void sincronizaCatalogosConUnaLecturaPorRepositorio() throws Exception {
        RolRepository rolRepository = mock(RolRepository.class);
        PermisoRepository permisoRepository = mock(PermisoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        EmpleadoRepository empleadoRepository = mock(EmpleadoRepository.class);
        TipoInsumoRepository tipoInsumoRepository = mock(TipoInsumoRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        List<PermisoModel> permisos = PermisoCatalog.DEFINITIONS.stream()
                .map(definition -> {
                    PermisoModel permiso = new PermisoModel();
                    permiso.setCode(definition.code());
                    return permiso;
                })
                .toList();
        RolModel admin = new RolModel();
        admin.setName("ADMIN");
        admin.setSistema(true);
        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setTelefono("7712345678");
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail("dev.mobilesco@outlook.com");

        when(permisoRepository.findAll()).thenReturn(permisos);
        when(permisoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(rolRepository.findAll()).thenReturn(List.of(admin));
        when(rolRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(empleadoRepository.findByTelefono("7712345678")).thenReturn(Optional.of(empleado));
        when(usuarioRepository.findByEmail("dev.mobilesco@outlook.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tipoInsumoRepository.findAll()).thenReturn(tiposBase());
        when(tipoInsumoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        new DataSeeder().initData(
                rolRepository,
                permisoRepository,
                usuarioRepository,
                empleadoRepository,
                tipoInsumoRepository,
                passwordEncoder).run();

        verify(permisoRepository, times(1)).findAll();
        verify(rolRepository, times(1)).findAll();
        verify(tipoInsumoRepository, times(1)).findAll();
        verify(permisoRepository, never()).findByCode(any());
        verify(rolRepository, never()).findByName(any());
        verify(tipoInsumoRepository, never()).findByCodigoIgnoreCase(any());
    }

    private List<TipoInsumoModel> tiposBase() {
        return List.of("HERRAJES", "PLASTICOS", "CARPINTERIA", "PINTURA", "TAPICERIA").stream()
                .map(codigo -> {
                    TipoInsumoModel tipo = new TipoInsumoModel();
                    tipo.setCodigo(codigo);
                    return tipo;
                })
                .toList();
    }
}
