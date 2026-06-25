package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.PermisoModel;

public interface PermisoRepository extends JpaRepository<PermisoModel, Long> {
    Optional<PermisoModel> findByCode(String code);
    boolean existsByCode(String code);
    List<PermisoModel> findByCodeIn(Collection<String> codes);
    List<PermisoModel> findByActivoTrueOrderByModuloAscNombreAsc();
}
