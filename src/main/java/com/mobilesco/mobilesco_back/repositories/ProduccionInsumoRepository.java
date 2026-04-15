package com.mobilesco.mobilesco_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.ProduccionInsumoModel;

@Repository
public interface ProduccionInsumoRepository extends JpaRepository<ProduccionInsumoModel, Long> {
    
    List<ProduccionInsumoModel> findByProduccionId(Long produccionId);
    
    List<ProduccionInsumoModel> findByInsumoId(Long insumoId);
    
    @Query("SELECT SUM(pi.costoTotal) FROM ProduccionInsumoModel pi WHERE pi.produccion.id = :produccionId")
    Double sumCostoInsumosByProduccion(@Param("produccionId") Long produccionId);
}