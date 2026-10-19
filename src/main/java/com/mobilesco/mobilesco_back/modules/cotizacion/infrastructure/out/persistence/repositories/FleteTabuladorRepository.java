package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.FleteTabuladorModel;

public interface FleteTabuladorRepository extends JpaRepository<FleteTabuladorModel, Long> {
    List<FleteTabuladorModel> findAllByOrderByHastaKmAsc();

    Optional<FleteTabuladorModel> findFirstByHastaKmGreaterThanEqualOrderByHastaKmAsc(BigDecimal distanciaKm);
}
