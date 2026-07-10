package com.mobilesco.mobilesco_back.modules.operacion.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
            SELECT o
            FROM OperacionModel o
            LEFT JOIN o.centroTrabajo c
            WHERE (:activo IS NULL OR o.activo = :activo)
              AND (:centroTrabajo IS NULL OR c.nombre = :centroTrabajo)
              AND (
                    :busqueda IS NULL
                    OR LOWER(o.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(o.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(o.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
            """)
    Page<OperacionModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            @Param("centroTrabajo") String centroTrabajo,
            Pageable pageable);
}
