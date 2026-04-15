package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.enums.TipoCostoIndirecto;
import com.mobilesco.mobilesco_back.models.CostoIndirectoModel;

@Repository
public interface CostoIndirectoRepository extends JpaRepository<CostoIndirectoModel, Long> {
    
    Optional<CostoIndirectoModel> findByCodigo(String codigo);
    
    boolean existsByCodigoIgnoreCase(String codigo);
    
    List<CostoIndirectoModel> findByActivoTrue();
    
    List<CostoIndirectoModel> findByTipo(TipoCostoIndirecto tipo);
    
    List<CostoIndirectoModel> findByBaseDistribucion(BaseDistribucion base);
    
    @Query("SELECT c FROM CostoIndirectoModel c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<CostoIndirectoModel> buscarPorNombre(@Param("nombre") String nombre);
}