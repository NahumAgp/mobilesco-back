package com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.EstadoRequisicionAlmacen;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.RequisicionAlmacenModel;

public interface RequisicionAlmacenRepository extends JpaRepository<RequisicionAlmacenModel, Long> {

    @Query("""
            SELECT r FROM RequisicionAlmacenModel r
            WHERE (:solicitanteId IS NULL OR r.solicitante.id = :solicitanteId)
              AND (:estado IS NULL OR r.estado = :estado)
              AND (:busqueda IS NULL
                   OR LOWER(r.folio) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(r.solicitanteNombre) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<RequisicionAlmacenModel> buscar(
            @Param("solicitanteId") Long solicitanteId,
            @Param("estado") EstadoRequisicionAlmacen estado,
            @Param("busqueda") String busqueda,
            Pageable pageable);

    @EntityGraph(attributePaths = {"solicitante", "detalles", "detalles.insumo"})
    Optional<RequisicionAlmacenModel> findWithDetailsById(Long id);
}
