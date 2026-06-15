package com.mobilesco.mobilesco_back.modules.operacion.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;

public interface OperacionRepository extends JpaRepository<OperacionModel, Long> {
    
    // Buscar por código
    Optional<OperacionModel> findByCodigo(String codigo);
    
    // Verificar si existe por código
    boolean existsByCodigoIgnoreCase(String codigo);
    
    // Listar activos
    List<OperacionModel> findByActivoTrue();
    
    // Buscar por nombre (búsqueda)
    @Query("SELECT o FROM OperacionModel o WHERE LOWER(o.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<OperacionModel> buscarPorNombre(@Param("nombre") String nombre);
    
    // Filtrar por centro de trabajo
    List<OperacionModel> findByCentroTrabajoId(Long centroTrabajoId);
}
