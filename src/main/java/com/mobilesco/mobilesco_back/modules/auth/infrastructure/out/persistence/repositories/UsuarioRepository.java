package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✅ este es el que usa /me, y debe regresar UsuarioModel, NO Object
    @EntityGraph(attributePaths = {"roles", "empleado"})
    Optional<UsuarioModel> findOneByEmail(String email);

    Optional<UsuarioModel> findByEmpleado(EmpleadoModel empleado);
}