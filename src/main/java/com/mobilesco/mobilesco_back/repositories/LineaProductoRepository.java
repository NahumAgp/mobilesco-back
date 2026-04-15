package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.LineaProductoModel;

@Repository
public interface LineaProductoRepository extends JpaRepository<LineaProductoModel, Long> {
    
    Optional<LineaProductoModel> findByNombre(String nombre);
    
    List<LineaProductoModel> findByActivoTrue();
    
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LineaProductoModel l WHERE LOWER(l.nombre) = LOWER(:nombre)")
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT l FROM LineaProductoModel l WHERE LOWER(l.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND l.activo = true")
    List<LineaProductoModel> buscarPorNombre(@Param("nombre") String nombre);
}