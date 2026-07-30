package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.CotizacionModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion;

public interface CotizacionRepository extends JpaRepository<CotizacionModel, Long> {
    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM CotizacionModel c
            JOIN c.detalles d
            WHERE d.producto.id IN :productoIds
            """)
    boolean existsByProductoIdsInCotizaciones(@Param("productoIds") Collection<Long> productoIds);

    @Query("""
            SELECT c FROM CotizacionModel c
            LEFT JOIN c.cliente cliente
            WHERE (:estado IS NULL OR c.estado = :estado)
              AND (:busqueda IS NULL
                   OR LOWER(c.folio) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(COALESCE(cliente.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(COALESCE(cliente.razonSocial, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<CotizacionModel> buscar(@Param("estado") EstadoCotizacion estado,
            @Param("busqueda") String busqueda, Pageable pageable);
}
