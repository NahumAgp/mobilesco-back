package com.mobilesco.mobilesco_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.ProduccionTiempoModel;

@Repository
public interface ProduccionTiempoRepository extends JpaRepository<ProduccionTiempoModel, Long> {
    
    List<ProduccionTiempoModel> findByProduccionId(Long produccionId);
    
    List<ProduccionTiempoModel> findByOperacionId(Long operacionId);
    
    List<ProduccionTiempoModel> findByCentroTrabajoId(Long centroTrabajoId);
    
    @Query("SELECT SUM(pt.costoTotal) FROM ProduccionTiempoModel pt WHERE pt.produccion.id = :produccionId")
    Double sumCostoMODByProduccion(@Param("produccionId") Long produccionId);
    
    @Query("SELECT SUM(pt.minutosReales) FROM ProduccionTiempoModel pt WHERE pt.produccion.id = :produccionId")
    Double sumTiempoTotalByProduccion(@Param("produccionId") Long produccionId);
}