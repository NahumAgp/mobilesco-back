package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.MaterialModel;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialModel, Long> {
    
    Optional<MaterialModel> findByNombre(String nombre);
    
    List<MaterialModel> findByActivoTrue();
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MaterialModel m WHERE LOWER(m.nombre) = LOWER(:nombre)")
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT m FROM MaterialModel m WHERE LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND m.activo = true")
    List<MaterialModel> buscarPorNombre(@Param("nombre") String nombre);
}