package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.EmpleadoModel;

@Repository
public interface EmpleadoRepository extends JpaRepository<EmpleadoModel, Long> {

    List<EmpleadoModel> findByActivo(Boolean activo);

    List<EmpleadoModel> findByNombreContainingIgnoreCase(String nombre);

    List<EmpleadoModel> findByActivoAndNombreContainingIgnoreCase(Boolean activo, String nombre);
    Optional<EmpleadoModel> findByTelefono(String telefono);

}