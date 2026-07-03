package com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.areatrabajo.domain.models.AreaTrabajoModel;

public interface AreaTrabajoRepository extends JpaRepository<AreaTrabajoModel, Long> {
    Optional<AreaTrabajoModel> findByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByNombreIgnoreCase(String nombre);
    List<AreaTrabajoModel> findByActivoTrueOrderByNombreAsc();
    List<AreaTrabajoModel> findAllByOrderByNombreAsc();
}
