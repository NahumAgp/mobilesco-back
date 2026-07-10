package com.mobilesco.mobilesco_back.modules.centrotrabajo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.centrotrabajo.domain.models.CentroTrabajoModel;

public interface CentroTrabajoRepository extends JpaRepository<CentroTrabajoModel, Long> {
    
    Optional<CentroTrabajoModel> findByCodigo(String codigo);
    
    boolean existsByCodigoIgnoreCase(String codigo);
    
    List<CentroTrabajoModel> findByActivoTrue();
    
    @Query("SELECT c FROM CentroTrabajoModel c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<CentroTrabajoModel> buscarPorNombre(@Param("nombre") String nombre);

    @Query("""
            SELECT c FROM CentroTrabajoModel c
            WHERE (:busqueda IS NULL OR :busqueda = '' OR
                   LOWER(COALESCE(c.codigo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(c.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(c.unidadCapacidad, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(c.enlaceDriveReporte, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')))
              AND (:estatus IS NULL OR :estatus = '' OR :estatus = 'TODOS' OR
                   (:estatus = 'ACTIVO' AND c.activo = true) OR
                   (:estatus = 'INACTIVO' AND c.activo = false))
              AND (:soloActivos = false OR c.activo = true)
            """)
    Page<CentroTrabajoModel> buscarPaginado(
            @Param("busqueda") String busqueda,
            @Param("estatus") String estatus,
            @Param("soloActivos") boolean soloActivos,
            Pageable pageable);
}
