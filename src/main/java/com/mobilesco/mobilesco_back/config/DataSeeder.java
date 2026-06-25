package com.mobilesco.mobilesco_back.config;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.auth.application.usecases.PermisoCatalog;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.PermisoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.empleado.infrastructure.out.persistence.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.PermisoRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.RolRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.out.persistence.repositories.TipoInsumoRepository;

@Configuration
public class DataSeeder {

    @Bean
    @Order(1)
    @SuppressWarnings("unused")
    CommandLineRunner initData(
            RolRepository roleRepo,
            PermisoRepository permisoRepository,
            UsuarioRepository userRepo,
            EmpleadoRepository empleadoRepo,
            TipoInsumoRepository tipoInsumoRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ===============================
            // 1️⃣ Crear roles si no existen
            // ===============================

            PermisoCatalog.DEFINITIONS.forEach(definition -> permisoRepository.findByCode(definition.code()).orElseGet(() -> {
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

            RolModel adminRole = roleRepo.findByName("ADMIN").orElseGet(() -> {
                RolModel r = new RolModel();
                r.setName("ADMIN");
                r.setSistema(true);
                return roleRepo.save(r);
            });

            RolModel employeeRole = roleRepo.findByName("EMPLOYEE").orElseGet(() -> {
                RolModel r = new RolModel();
                r.setName("EMPLOYEE");
                r.setSistema(true);
                return roleRepo.save(r);
            });

            List.of(
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
            ).forEach(nombreRol -> roleRepo.findByName(nombreRol).orElseGet(() -> {
                RolModel r = new RolModel();
                r.setName(nombreRol);
                r.setSistema(true);
                return roleRepo.save(r);
            }));

            roleRepo.findAll().forEach(rol -> {
                Set<String> codigosDefault = PermisoCatalog.DEFAULT_ROLE_PERMISSIONS.getOrDefault(rol.getName(), Set.of());
                if (!codigosDefault.isEmpty() && rol.getPermisos().isEmpty()) {
                    rol.setPermisos(new HashSet<>(permisoRepository.findByCodeIn(codigosDefault)));
                    roleRepo.save(rol);
                }
            });

            // ===============================
            // 2️⃣ Crear empleado
            // ===============================

            String telefono = "7712345678";

            EmpleadoModel empleado = empleadoRepo.findByTelefono(telefono)
                    .orElseGet(() -> {

                        EmpleadoModel e = new EmpleadoModel();

                        e.setNombre("Nahum");
                        e.setApellidoPaterno("Aguilar");
                        e.setApellidoMaterno("Perez");
                        e.setTelefono(telefono);
                        e.setActivo(true);

                        e.setFechaNacimiento(
                                LocalDate.of(2003, 3, 25)
                        );

                        EmpleadoModel saved = empleadoRepo.save(e);

                        System.out.println("✅ Empleado creado: Nahum Aguilar Perez");

                        return saved;
                    });

            // ===============================
            // 3️⃣ Crear usuario asociado
            // ===============================

            String email = "dev.mobilesco@outlook.com";

            if (!userRepo.existsByEmail(email)) {

                UsuarioModel u = new UsuarioModel();

                u.setEmail(email);
                u.setPasswordHash(passwordEncoder.encode("Admin123!"));

                u.setEnabled(true);
                u.setLocked(false);
                u.setEstadoCuenta(com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario.ACTIVE);

                // relación con empleado
                u.setEmpleado(empleado);

                // rol ADMIN
                u.setRoles(Set.of(adminRole));

                userRepo.save(u);

                System.out.println("✅ Usuario creado: " + email + " / Admin123!");

            } else {

                userRepo.findByEmail(email).ifPresent(u -> {
                    if (u.getEstadoCuenta() == null) {
                        u.setEstadoCuenta(com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario.ACTIVE);
                        u.setEnabled(true);
                        u.setLocked(false);
                        userRepo.save(u);
                    }
                });

                System.out.println("ℹ️ Usuario ya existe: " + email);

            }

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
        };
    }
}
