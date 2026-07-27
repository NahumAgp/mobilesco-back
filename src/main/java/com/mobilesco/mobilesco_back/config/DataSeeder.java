package com.mobilesco.mobilesco_back.config;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.PermisoCatalog;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario;
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

@Configuration
public class DataSeeder {

    private static final String DEV_EMAIL = "dev.mobilesco@outlook.com";
    private static final String DEV_PASSWORD = "Admin123!";
    private static final String DEV_PHONE = "7712345678";
    private static final Map<String, Set<String>> PERMISOS_OBLIGATORIOS_POR_ROL = Map.of(
            "JEFE_ALMACEN", Set.of("VIEW_WAREHOUSE_REQUISITIONS", "ACTION_STOCK_ADJUSTMENTS")
    );

    @Bean
    @Order(1)
    CommandLineRunner initData(
            RolRepository roleRepo,
            PermisoRepository permisoRepository,
            UsuarioRepository userRepo,
            EmpleadoRepository empleadoRepo,
            TipoInsumoRepository tipoInsumoRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            seedPermisos(permisoRepository);
            RolModel adminRole = seedRoles(roleRepo);
            asignarPermisosDefault(roleRepo, permisoRepository);
            asegurarPermisosObligatorios(roleRepo, permisoRepository);
            EmpleadoModel empleadoDev = seedEmpleadoDev(empleadoRepo);
            seedUsuarioDev(userRepo, passwordEncoder, adminRole, empleadoDev);
            seedTiposInsumo(tipoInsumoRepository);
        };
    }

    private void seedPermisos(PermisoRepository permisoRepository) {
        PermisoCatalog.DEFINITIONS.forEach(definition ->
                permisoRepository.findByCode(definition.code()).orElseGet(() -> {
                    PermisoModel permiso = new PermisoModel();
                    permiso.setCode(definition.code());
                    permiso.setNombre(definition.nombre());
                    permiso.setModulo(definition.modulo());
                    permiso.setVista(definition.vista());
                    permiso.setDescripcion(definition.descripcion());
                    permiso.setRuta(definition.ruta());
                    permiso.setTipo(definition.tipo());
                    permiso.setActivo(true);
                    return permisoRepository.save(permiso);
                }));
    }

    private RolModel seedRoles(RolRepository roleRepo) {
        RolModel adminRole = seedRolSistema(roleRepo, "ADMIN");

        List.of(
                "EMPLOYEE",
                "DIRECTOR_GENERAL",
                "SUBDIRECCION_ADMINISTRATIVA",
                "ASISTENTE_GERENCIAL",
                "SUPERVISOR_PRODUCCION",
                "JEFE_HERRERIA",
                "JEFE_CARPINTERIA",
                "JEFE_ARMADO",
                "JEFE_ALMACEN",
                "JEFE_LOGISTICA",
                "TECNICO",
                "AYUDANTE_GENERAL"
        ).forEach(nombreRol -> seedRolSistema(roleRepo, nombreRol));

        return adminRole;
    }

    private RolModel seedRolSistema(RolRepository roleRepo, String nombre) {
        return roleRepo.findByName(nombre).orElseGet(() -> {
            RolModel rol = new RolModel();
            rol.setName(nombre);
            rol.setSistema(true);
            return roleRepo.save(rol);
        });
    }

    private void asignarPermisosDefault(RolRepository roleRepo, PermisoRepository permisoRepository) {
        roleRepo.findAll().forEach(rol -> {
            Set<String> codigosDefault = PermisoCatalog.DEFAULT_ROLE_PERMISSIONS.getOrDefault(rol.getName(), Set.of());
            if (!codigosDefault.isEmpty() && rol.getPermisos().isEmpty()) {
                rol.setPermisos(new HashSet<>(permisoRepository.findByCodeIn(codigosDefault)));
                roleRepo.save(rol);
            }
        });
    }

    private void asegurarPermisosObligatorios(
            RolRepository roleRepo,
            PermisoRepository permisoRepository) {
        PERMISOS_OBLIGATORIOS_POR_ROL.forEach((nombreRol, codigos) ->
                roleRepo.findByName(nombreRol).ifPresent(rol -> {
                    Set<PermisoModel> permisos = new HashSet<>(rol.getPermisos());
                    boolean cambio = permisos.addAll(permisoRepository.findByCodeIn(codigos));
                    if (cambio) {
                        rol.setPermisos(permisos);
                        roleRepo.save(rol);
                    }
                }));
    }

    private EmpleadoModel seedEmpleadoDev(EmpleadoRepository empleadoRepo) {
        return empleadoRepo.findByTelefono(DEV_PHONE).orElseGet(() -> {
            EmpleadoModel empleado = new EmpleadoModel();
            empleado.setNombre("Nahum");
            empleado.setApellidoPaterno("Aguilar");
            empleado.setApellidoMaterno("Perez");
            empleado.setTelefono(DEV_PHONE);
            empleado.setFechaNacimiento(LocalDate.of(2003, 3, 25));
            empleado.setActivo(true);
            return empleadoRepo.save(empleado);
        });
    }

    private void seedUsuarioDev(
            UsuarioRepository userRepo,
            PasswordEncoder passwordEncoder,
            RolModel adminRole,
            EmpleadoModel empleadoDev) {

        UsuarioModel usuario = userRepo.findByEmail(DEV_EMAIL).orElseGet(() -> {
            UsuarioModel nuevo = new UsuarioModel();
            nuevo.setEmail(DEV_EMAIL);
            nuevo.setPasswordHash(passwordEncoder.encode(DEV_PASSWORD));
            return nuevo;
        });

        usuario.setEnabled(true);
        usuario.setLocked(false);
        usuario.setEstadoCuenta(EstadoCuentaUsuario.ACTIVE);
        usuario.setEmpleado(empleadoDev);
        usuario.setRoles(Set.of(adminRole));

        userRepo.save(usuario);
    }

    private void seedTiposInsumo(TipoInsumoRepository tipoInsumoRepository) {
        List.of(
                new String[] {"HERRAJES", "Herrajes"},
                new String[] {"PLASTICOS", "Plasticos"},
                new String[] {"CARPINTERIA", "Carpinteria"},
                new String[] {"PINTURA", "Pintura"},
                new String[] {"TAPICERIA", "Tapiceria"}
        ).forEach(tipo -> tipoInsumoRepository.findByCodigoIgnoreCase(tipo[0]).orElseGet(() -> {
            TipoInsumoModel model = new TipoInsumoModel();
            model.setCodigo(tipo[0]);
            model.setNombre(tipo[1]);
            model.setActivo(true);
            return tipoInsumoRepository.save(model);
        }));
    }
}
