package com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;

public interface CompraRepository extends JpaRepository<CompraModel, Long> {

    List<CompraModel> findByActivoTrue();
    
    List<CompraModel> findByProveedorIdAndActivoTrue(Long proveedorId);
    
    List<CompraModel> findByEstadoAndActivoTrue(String estado);
    
    @Query("SELECT c FROM CompraModel c WHERE c.activo = true AND c.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    List<CompraModel> findByRangoFechas(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    @Query("SELECT c FROM CompraModel c WHERE c.activo = true AND c.proveedor.id = :proveedorId AND c.estado = :estado")
    List<CompraModel> findByProveedorAndEstado(
        @Param("proveedorId") Long proveedorId,
        @Param("estado") String estado
    );
    
    @Query("SELECT c FROM CompraModel c WHERE c.activo = true AND c.folio LIKE %:folio%")
    List<CompraModel> buscarPorFolio(@Param("folio") String folio);

    @Query("""
            SELECT c FROM CompraModel c
            JOIN c.proveedor p
            WHERE c.activo = true
              AND (:busqueda IS NULL OR :busqueda = '' OR
                   LOWER(c.folio) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(c.metodoPago, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(c.numeroDocumento, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(c.entregadoPor, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(p.razonSocial, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(p.rfc, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')))
              AND (
                   :estado IS NULL OR :estado = '' OR :estado = 'TODOS'
                   OR (:estado <> 'POR_CERRAR' AND c.estado = :estado)
                   OR (
                       :estado = 'POR_CERRAR'
                       AND c.estado NOT IN ('CANCELADA', 'RECIBIDA')
                       AND EXISTS (
                           SELECT d1.id FROM DetalleCompraModel d1
                           WHERE d1.compra = c
                       )
                       AND NOT EXISTS (
                           SELECT d2.id FROM DetalleCompraModel d2
                           WHERE d2.compra = c
                             AND COALESCE(d2.cantidadRecibida, 0) < COALESCE(d2.cantidad, 0)
                       )
                   )
              )
              AND (:proveedor IS NULL OR :proveedor = '' OR p.razonSocial = :proveedor)
              AND (:fechaInicio IS NULL OR c.fechaCompra >= :fechaInicio)
              AND (:fechaFin IS NULL OR c.fechaCompra <= :fechaFin)
            """)
    Page<CompraModel> buscarPaginado(
        @Param("busqueda") String busqueda,
        @Param("estado") String estado,
        @Param("proveedor") String proveedor,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        Pageable pageable
    );

    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByProveedorId(Long proveedorId);

    
}
