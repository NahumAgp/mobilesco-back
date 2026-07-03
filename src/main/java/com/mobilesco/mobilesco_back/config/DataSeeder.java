package com.mobilesco.mobilesco_back.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.PermisoCatalog;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.PermisoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.PermisoRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.RolRepository;
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
            TipoInsumoRepository tipoInsumoRepository) {

        return args -> {
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

            roleRepo.findByName("ADMIN").orElseGet(() -> crearRolSistema(roleRepo, "ADMIN"));
            roleRepo.findByName("EMPLOYEE").orElseGet(() -> crearRolSistema(roleRepo, "EMPLOYEE"));

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
            ).forEach(nombreRol -> roleRepo.findByName(nombreRol).orElseGet(() -> crearRolSistema(roleRepo, nombreRol)));

            roleRepo.findAll().forEach(rol -> {
                Set<String> codigosDefault = PermisoCatalog.DEFAULT_ROLE_PERMISSIONS.getOrDefault(rol.getName(), Set.of());
                if (!codigosDefault.isEmpty() && rol.getPermisos().isEmpty()) {
                    rol.setPermisos(new HashSet<>(permisoRepository.findByCodeIn(codigosDefault)));
                    roleRepo.save(rol);
                }
            });

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

    private RolModel crearRolSistema(RolRepository roleRepo, String nombre) {
        RolModel rol = new RolModel();
        rol.setName(nombre);
        rol.setSistema(true);
        return roleRepo.save(rol);
    }
}
