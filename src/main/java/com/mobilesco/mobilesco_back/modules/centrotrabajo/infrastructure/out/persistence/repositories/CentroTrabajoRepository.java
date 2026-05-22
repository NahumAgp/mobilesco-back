package com.mobilesco.mobilesco_back.modules.centrotrabajo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.modules.centrotrabajo.domain.models.CentroTrabajoModel;

@Repository
public interface CentroTrabajoRepository extends JpaRepository<CentroTrabajoModel, Long> {
    
    Optional<CentroTrabajoModel> findByCodigo(String codigo);
    
    boolean existsByCodigoIgnoreCase(String codigo);
    
    List<CentroTrabajoModel> findByActivoTrue();
    
    @Query("SELECT c FROM CentroTrabajoModel c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<CentroTrabajoModel> buscarPorNombre(@Param("nombre") String nombre);
}