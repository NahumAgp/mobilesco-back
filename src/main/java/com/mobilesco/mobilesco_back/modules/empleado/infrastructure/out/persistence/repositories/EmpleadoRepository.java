package com.mobilesco.mobilesco_back.modules.empleado.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;

public interface EmpleadoRepository extends JpaRepository<EmpleadoModel, Long> {

    List<EmpleadoModel> findByActivo(Boolean activo);

    List<EmpleadoModel> findByNombreContainingIgnoreCase(String nombre);

    List<EmpleadoModel> findByActivoAndNombreContainingIgnoreCase(Boolean activo, String nombre);
    Optional<EmpleadoModel> findByTelefono(String telefono);

}
