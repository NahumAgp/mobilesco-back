package com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelOperacionModel;

public interface NivelOperacionRepository extends JpaRepository<NivelOperacionModel, Long> {

    List<NivelOperacionModel> findByNivelIdOrderByOrdenAsc(Long nivelId);

    @Modifying
    @Query("DELETE FROM NivelOperacionModel no WHERE no.nivel.id = :nivelId")
    void deleteByNivelId(@Param("nivelId") Long nivelId);
}
