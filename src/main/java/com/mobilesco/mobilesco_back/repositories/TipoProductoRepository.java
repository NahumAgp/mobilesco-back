package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.TipoProductoModel;

@Repository
public interface TipoProductoRepository extends JpaRepository<TipoProductoModel, Long> {
    
    Optional<TipoProductoModel> findByNombre(String nombre);
    
    List<TipoProductoModel> findByActivoTrue();
    
    List<TipoProductoModel> findByFamiliaIdAndActivoTrue(Long familiaId);
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TipoProductoModel t WHERE LOWER(t.nombre) = LOWER(:nombre)")
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT t FROM TipoProductoModel t WHERE LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND t.activo = true")
    List<TipoProductoModel> buscarPorNombre(@Param("nombre") String nombre);
}