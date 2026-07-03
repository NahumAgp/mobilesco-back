package com.mobilesco.mobilesco_back.modules.empleado.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;

public interface EmpleadoRepository extends JpaRepository<EmpleadoModel, Long> {

    @Override
    @EntityGraph(attributePaths = "areaTrabajo")
    List<EmpleadoModel> findAll();

    @Override
    @EntityGraph(attributePaths = "areaTrabajo")
    Optional<EmpleadoModel> findById(Long id);

    @EntityGraph(attributePaths = "areaTrabajo")
    List<EmpleadoModel> findByActivo(Boolean activo);

    @EntityGraph(attributePaths = "areaTrabajo")
    List<EmpleadoModel> findByNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = "areaTrabajo")
    List<EmpleadoModel> findByActivoAndNombreContainingIgnoreCase(Boolean activo, String nombre);

    Optional<EmpleadoModel> findByTelefono(String telefono);

}
