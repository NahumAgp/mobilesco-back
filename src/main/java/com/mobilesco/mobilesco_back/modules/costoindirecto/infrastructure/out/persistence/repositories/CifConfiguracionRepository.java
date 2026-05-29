package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.models.CifConfiguracionModel;

@Repository
public interface CifConfiguracionRepository extends JpaRepository<CifConfiguracionModel, Long> {
    Optional<CifConfiguracionModel> findFirstByActivoTrueOrderByIdAsc();
}
