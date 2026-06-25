package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✅ este es el que usa /me, y debe regresar UsuarioModel, NO Object
    @EntityGraph(attributePaths = {"roles", "roles.permisos", "permisos", "empleado"})
    Optional<UsuarioModel> findOneByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"roles", "roles.permisos", "permisos", "empleado"})
    java.util.List<UsuarioModel> findAll();

    Optional<UsuarioModel> findByEmpleado(EmpleadoModel empleado);
}
