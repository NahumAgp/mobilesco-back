package com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.CotizacionModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion;

public interface CotizacionRepository extends JpaRepository<CotizacionModel, Long> {
    interface ResumenPeriodoProjection {
        Long getCantidadActual();
        BigDecimal getMontoActual();
        Long getCierresActuales();
        Long getDecisionesActuales();
        BigDecimal getMontoAnterior();
    }

    interface CotizacionRecienteProjection {
        Long getId();
        String getFolio();
        String getClienteNombre();
        EstadoCotizacion getEstado();
        BigDecimal getTotal();
        LocalDateTime getFechaRegistro();
        Long getPartidas();
        Long getUnidades();
    }

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

    @Query("""
            SELECT
              SUM(CASE WHEN c.fechaRegistro >= :desde AND c.fechaRegistro < :hasta THEN 1 ELSE 0 END) AS cantidadActual,
              COALESCE(SUM(CASE WHEN c.fechaRegistro >= :desde AND c.fechaRegistro < :hasta
                               AND c.estado <> com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion.CANCELADA
                           THEN c.total ELSE 0 END), 0) AS montoActual,
              SUM(CASE WHEN c.fechaRegistro >= :desde AND c.fechaRegistro < :hasta
                               AND c.estado IN (com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion.ACEPTADA,
                                                com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion.COMPLETADA)
                           THEN 1 ELSE 0 END) AS cierresActuales,
              SUM(CASE WHEN c.fechaRegistro >= :desde AND c.fechaRegistro < :hasta
                               AND c.estado IN (com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion.ACEPTADA,
                                                com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion.COMPLETADA,
                                                com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion.RECHAZADA)
                           THEN 1 ELSE 0 END) AS decisionesActuales,
              COALESCE(SUM(CASE WHEN c.fechaRegistro >= :desdeAnterior AND c.fechaRegistro < :desde
                               AND c.estado <> com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion.CANCELADA
                           THEN c.total ELSE 0 END), 0) AS montoAnterior
            FROM CotizacionModel c
            WHERE c.fechaRegistro >= :desdeAnterior AND c.fechaRegistro < :hasta
            """)
    ResumenPeriodoProjection resumirPeriodo(
            @Param("desdeAnterior") LocalDateTime desdeAnterior,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    long countByEstadoIn(List<EstadoCotizacion> estados);

    long countByEstadoInAndFechaVencimientoBefore(List<EstadoCotizacion> estados, LocalDate fecha);

    long countByEstadoInAndFechaVencimientoBetween(
            List<EstadoCotizacion> estados, LocalDate desde, LocalDate hasta);

    @Query("""
            SELECT c.id AS id, c.folio AS folio,
                   COALESCE(cliente.nombreComercial, cliente.razonSocial, cliente.nombre, 'Público general') AS clienteNombre,
                   c.estado AS estado, c.total AS total, c.fechaRegistro AS fechaRegistro,
                   COUNT(d.id) AS partidas, COALESCE(SUM(d.cantidad), 0) AS unidades
            FROM CotizacionModel c
            LEFT JOIN c.cliente cliente
            LEFT JOIN c.detalles d
            GROUP BY c.id, c.folio, cliente.nombreComercial, cliente.razonSocial, cliente.nombre,
                     c.estado, c.total, c.fechaRegistro
            ORDER BY c.fechaRegistro DESC, c.id DESC
            """)
    List<CotizacionRecienteProjection> encontrarRecientes(Pageable pageable);
}
