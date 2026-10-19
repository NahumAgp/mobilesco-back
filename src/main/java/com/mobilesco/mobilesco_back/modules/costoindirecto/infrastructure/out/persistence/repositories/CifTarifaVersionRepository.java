package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.out.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.models.CifTarifaVersionModel;

public interface CifTarifaVersionRepository extends JpaRepository<CifTarifaVersionModel, Long> {
}
