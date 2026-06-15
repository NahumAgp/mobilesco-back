package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.SalidaInsumoModel;

public interface SalidaInsumoRepository extends JpaRepository<SalidaInsumoModel, Long> {
    List<SalidaInsumoModel> findAllByOrderByFechaSalidaDesc();
}
