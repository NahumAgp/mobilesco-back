package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.models.CifConfiguracionModel;

public interface CifConfiguracionRepository extends JpaRepository<CifConfiguracionModel, Long> {
    Optional<CifConfiguracionModel> findFirstByActivoTrueOrderByIdAsc();
}
