package com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.areatrabajo.domain.models.AreaTrabajoModel;

public interface AreaTrabajoRepository extends JpaRepository<AreaTrabajoModel, Long> {
    Optional<AreaTrabajoModel> findByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByNombreIgnoreCase(String nombre);
    List<AreaTrabajoModel> findByActivoTrueOrderByNombreAsc();
    List<AreaTrabajoModel> findAllByOrderByNombreAsc();

    @Query("""
            SELECT a
            FROM AreaTrabajoModel a
            WHERE (:activo IS NULL OR a.activo = :activo)
              AND (
                    :busqueda IS NULL
                    OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(a.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
            """)
    Page<AreaTrabajoModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
