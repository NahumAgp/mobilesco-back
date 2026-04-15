package com.mobilesco.mobilesco_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.DistribucionCostoModel;

@Repository
public interface DistribucionCostoRepository extends JpaRepository<DistribucionCostoModel, Long> {
    
    List<DistribucionCostoModel> findByAnioAndMes(Integer anio, Integer mes);
    
    List<DistribucionCostoModel> findByProductoId(Long productoId);
    
    List<DistribucionCostoModel> findByCostoIndirectoId(Long costoIndirectoId);
    
    List<DistribucionCostoModel> findByProductoIdAndAnioAndMes(Long productoId, Integer anio, Integer mes);
    
    @Query("SELECT SUM(d.montoAsignado) FROM DistribucionCostoModel d WHERE d.producto.id = :productoId AND d.anio = :anio AND d.mes = :mes")
    Double sumByProductoAndPeriodo(@Param("productoId") Long productoId, 
                                   @Param("anio") Integer anio, 
                                   @Param("mes") Integer mes);
    
    @Query("SELECT d FROM DistribucionCostoModel d WHERE d.anio = :anio AND d.mes = :mes ORDER BY d.producto.id, d.costoIndirecto.id")
    List<DistribucionCostoModel> findDistribucionCompleta(@Param("anio") Integer anio, @Param("mes") Integer mes);
    
    void deleteByAnioAndMes(Integer anio, Integer mes);
}