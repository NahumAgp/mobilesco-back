package com.mobilesco.mobilesco_back.config;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mobilesco.mobilesco_back.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.models.RolModel;
import com.mobilesco.mobilesco_back.models.UsuarioModel;
import com.mobilesco.mobilesco_back.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.repositories.RolRepository;
import com.mobilesco.mobilesco_back.repositories.UsuarioRepository;

@Configuration
public class DataSeeder {

    @Bean
    @SuppressWarnings("unused")
    CommandLineRunner initData(
            RolRepository roleRepo,
            UsuarioRepository userRepo,
            EmpleadoRepository empleadoRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ===============================
            // 1️⃣ Crear roles si no existen
            // ===============================

            RolModel adminRole = roleRepo.findByName("ADMIN").orElseGet(() -> {
                RolModel r = new RolModel();
                r.setName("ADMIN");
                return roleRepo.save(r);
            });

            RolModel employeeRole = roleRepo.findByName("EMPLOYEE").orElseGet(() -> {
                RolModel r = new RolModel();
                r.setName("EMPLOYEE");
                return roleRepo.save(r);
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

                // relación con empleado
                u.setEmpleado(empleado);

                // rol ADMIN
                u.setRoles(Set.of(adminRole));

                userRepo.save(u);

                System.out.println("✅ Usuario creado: " + email + " / Admin123!");

            } else {

                System.out.println("ℹ️ Usuario ya existe: " + email);

            }
        };
    }
}