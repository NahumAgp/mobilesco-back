package com.mobilesco.mobilesco_back.config;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            "JEFE_ALMACEN", Set.of("VIEW_WAREHOUSE_REQUISITIONS", "ACTION_STOCK_ADJUSTMENTS", "VIEW_PRODUCTION_ORDERS", "ACTION_PRODUCTION_MATERIAL_ISSUE"),
            "SUPERVISOR_PRODUCCION", Set.of("VIEW_PRODUCTION_ORDERS", "ACTION_PRODUCTION_ORDERS_CREATE", "ACTION_PRODUCTION_ORDERS_EDIT", "ACTION_PRODUCTION_ORDERS_RELEASE", "ACTION_PRODUCTION_PROGRESS", "ACTION_PRODUCTION_ORDERS_CANCEL")
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
            Map<String, PermisoModel> permisos = seedPermisos(permisoRepository);
            List<RolModel> roles = seedRoles(roleRepo);
            sincronizarPermisosRoles(roleRepo, roles, permisos);
            RolModel adminRole = roles.stream()
                    .filter(rol -> "ADMIN".equals(rol.getName()))
                    .findFirst()
                    .orElseThrow();
            EmpleadoModel empleadoDev = seedEmpleadoDev(empleadoRepo);
            seedUsuarioDev(userRepo, passwordEncoder, adminRole, empleadoDev);
            seedTiposInsumo(tipoInsumoRepository);
        };
    }

    private Map<String, PermisoModel> seedPermisos(PermisoRepository permisoRepository) {
        Map<String, PermisoModel> permisosPorCodigo = permisoRepository.findAll().stream()
                .collect(Collectors.toMap(PermisoModel::getCode, Function.identity()));
        List<PermisoModel> nuevos = new ArrayList<>();

        List<PermisoModel> actualizados = new ArrayList<>();
        PermisoCatalog.DEFINITIONS.forEach(definition -> {
            PermisoModel permiso = permisosPorCodigo.get(definition.code());
            if (permiso == null) {
                permiso = new PermisoModel();
                permiso.setCode(definition.code());
                nuevos.add(permiso);
            } else {
                actualizados.add(permiso);
            }
            permiso.setNombre(definition.nombre());
            permiso.setModulo(definition.modulo());
            permiso.setVista(definition.vista());
            permiso.setDescripcion(definition.descripcion());
            permiso.setRuta(definition.ruta());
            permiso.setTipo(definition.tipo());
            permiso.setActivo(true);
        });

        permisosPorCodigo.values().stream()
                .filter(permiso -> !PermisoCatalog.ALL_CODES.contains(permiso.getCode()))
                .filter(PermisoModel::isActivo)
                .forEach(permiso -> {
                    permiso.setActivo(false);
                    actualizados.add(permiso);
                });

        permisoRepository.saveAll(nuevos).forEach(permiso ->
                permisosPorCodigo.put(permiso.getCode(), permiso));
        permisoRepository.saveAll(actualizados);
        return permisosPorCodigo;
    }

    private List<RolModel> seedRoles(RolRepository roleRepo) {
        List<String> nombres = List.of(
                "ADMIN",
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
        );
        Map<String, RolModel> rolesPorNombre = roleRepo.findAll().stream()
                .collect(Collectors.toMap(RolModel::getName, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        List<RolModel> nuevos = nombres.stream()
                .filter(nombre -> !rolesPorNombre.containsKey(nombre))
                .map(nombre -> {
                    RolModel rol = new RolModel();
                    rol.setName(nombre);
                    rol.setSistema(true);
                    return rol;
                })
                .toList();
        roleRepo.saveAll(nuevos).forEach(rol -> rolesPorNombre.put(rol.getName(), rol));
        return new ArrayList<>(rolesPorNombre.values());
    }

    private void sincronizarPermisosRoles(
            RolRepository roleRepo,
            List<RolModel> roles,
            Map<String, PermisoModel> permisosPorCodigo) {
        List<RolModel> modificados = new ArrayList<>();
        roles.forEach(rol -> {
            Set<PermisoModel> permisos = new HashSet<>(rol.getPermisos());
            Set<String> codigosDefault = PermisoCatalog.DEFAULT_ROLE_PERMISSIONS.getOrDefault(rol.getName(), Set.of());
            boolean cambio = false;
            if (PermisoCatalog.FULL_ACCESS_ROLES.contains(rol.getName())) {
                Set<PermisoModel> completos = resolverPermisos(PermisoCatalog.ALL_CODES, permisosPorCodigo);
                if (!permisos.equals(completos)) {
                    permisos = new HashSet<>(completos);
                    cambio = true;
                }
            } else if (!codigosDefault.isEmpty() && permisos.isEmpty()) {
                cambio = permisos.addAll(resolverPermisos(codigosDefault, permisosPorCodigo));
            }
            Set<String> obligatorios = PERMISOS_OBLIGATORIOS_POR_ROL.getOrDefault(rol.getName(), Set.of());
            cambio |= permisos.addAll(resolverPermisos(obligatorios, permisosPorCodigo));
            if (cambio) {
                rol.setPermisos(permisos);
                modificados.add(rol);
            }
        });
        roleRepo.saveAll(modificados);
    }

    private Set<PermisoModel> resolverPermisos(
            Set<String> codigos,
            Map<String, PermisoModel> permisosPorCodigo) {
        return codigos.stream()
                .map(permisosPorCodigo::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
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
        Set<String> nombresExistentes = tipoInsumoRepository.findAll().stream()
                .map(TipoInsumoModel::getNombre)
                .map(TipoInsumoModel::normalizarNombre)
                .collect(Collectors.toSet());
        List<TipoInsumoModel> nuevos = List.of(
                new String[] {"HERRAJES", "Herrajes"},
                new String[] {"PLASTICOS", "Plasticos"},
                new String[] {"CARPINTERIA", "Carpinteria"},
                new String[] {"PINTURA", "Pintura"},
                new String[] {"TAPICERIA", "Tapiceria"}
        ).stream()
                .filter(tipo -> !nombresExistentes.contains(TipoInsumoModel.normalizarNombre(tipo[1])))
                .map(tipo -> {
                    TipoInsumoModel model = new TipoInsumoModel();
                    model.setCodigo(tipo[0]);
                    model.setNombre(tipo[1]);
                    model.setActivo(true);
                    return model;
                })
                .toList();
        tipoInsumoRepository.saveAll(nuevos);
    }
}
