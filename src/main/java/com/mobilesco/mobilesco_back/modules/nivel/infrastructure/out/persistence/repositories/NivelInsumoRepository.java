package com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelInsumoModel;

public interface NivelInsumoRepository extends JpaRepository<NivelInsumoModel, Long> {

    List<NivelInsumoModel> findByNivelIdOrderByInsumoNombreAsc(Long nivelId);

    @Modifying
    @Query("DELETE FROM NivelInsumoModel ni WHERE ni.nivel.id = :nivelId")
    void deleteByNivelId(@Param("nivelId") Long nivelId);
}
