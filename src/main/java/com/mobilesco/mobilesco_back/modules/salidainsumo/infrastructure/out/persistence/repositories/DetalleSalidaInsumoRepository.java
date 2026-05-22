package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.DetalleSalidaInsumoModel;

@Repository
public interface DetalleSalidaInsumoRepository extends JpaRepository<DetalleSalidaInsumoModel, Long> {
    List<DetalleSalidaInsumoModel> findBySalidaInsumoIdOrderByIdAsc(Long salidaInsumoId);
}
