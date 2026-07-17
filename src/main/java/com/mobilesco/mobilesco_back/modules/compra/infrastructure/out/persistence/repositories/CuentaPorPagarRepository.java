package com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.CuentaPorPagarModel;

public interface CuentaPorPagarRepository extends JpaRepository<CuentaPorPagarModel, Long> {
    Optional<CuentaPorPagarModel> findByCompraId(Long compraId);
    List<CuentaPorPagarModel> findByActivoTrueOrderByFechaCuentaDesc();
    List<CuentaPorPagarModel> findByEstadoAndActivoTrueOrderByFechaCuentaDesc(String estado);

    @Query("""
            SELECT c FROM CuentaPorPagarModel c
            JOIN c.compra compra
            JOIN c.proveedor proveedor
            WHERE c.activo = true
              AND (:estado IS NULL OR :estado = '' OR :estado = 'TODOS' OR c.estado = :estado)
              AND (:fechaInicio IS NULL OR c.fechaCuenta >= :fechaInicio)
              AND (:fechaFin IS NULL OR c.fechaCuenta <= :fechaFin)
              AND (:busqueda IS NULL OR :busqueda = '' OR
                   LOWER(COALESCE(compra.folio, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(proveedor.razonSocial, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(proveedor.rfc, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(c.estado, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(compra.metodoPago, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<CuentaPorPagarModel> buscarPaginado(
            @Param("estado") String estado,
            @Param("busqueda") String busqueda,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            Pageable pageable);

    @Query("""
            SELECT c FROM CuentaPorPagarModel c
            JOIN FETCH c.compra compra
            JOIN FETCH c.proveedor proveedor
            WHERE c.activo = true
              AND (:estado IS NULL OR :estado = '' OR :estado = 'TODOS' OR c.estado = :estado)
              AND (:fechaInicio IS NULL OR c.fechaCuenta >= :fechaInicio)
              AND (:fechaFin IS NULL OR c.fechaCuenta <= :fechaFin)
              AND (:busqueda IS NULL OR :busqueda = '' OR
                   LOWER(COALESCE(compra.folio, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(proveedor.razonSocial, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(proveedor.rfc, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(c.estado, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(compra.metodoPago, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            ORDER BY c.fechaCuenta DESC, c.id DESC
            """)
    List<CuentaPorPagarModel> buscarReporte(
            @Param("estado") String estado,
            @Param("busqueda") String busqueda,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
