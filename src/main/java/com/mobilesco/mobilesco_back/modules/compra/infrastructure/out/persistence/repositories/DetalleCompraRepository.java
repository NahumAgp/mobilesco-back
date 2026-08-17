package com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.DetalleCompraModel;

public interface DetalleCompraRepository extends JpaRepository<DetalleCompraModel, Long> {
    
    List<DetalleCompraModel> findByCompraId(Long compraId);
    
    List<DetalleCompraModel> findByInsumoId(Long insumoId);

    boolean existsByInsumoId(Long insumoId);
    
    @Query("SELECT d FROM DetalleCompraModel d WHERE d.insumo.id = :insumoId ORDER BY d.compra.fechaCompra DESC")
    List<DetalleCompraModel> findUltimasComprasByInsumo(@Param("insumoId") Long insumoId);

    @Query("SELECT d FROM DetalleCompraModel d WHERE d.insumo.id = :insumoId AND d.compra.estado = 'RECIBIDA' " +
           "ORDER BY d.compra.fechaRecepcion DESC, d.compra.fechaCompra DESC, d.id DESC")
    List<DetalleCompraModel> findUltimasComprasRecibidasByInsumo(@Param("insumoId") Long insumoId, Pageable pageable);

    @Query(value = """
            SELECT recientes.insumo_id, recientes.costo
            FROM (
                SELECT d.insumo_id,
                       d.precio_unitario / NULLIF(d.factor_conversion, 0) AS costo,
                       ROW_NUMBER() OVER (
                           PARTITION BY d.insumo_id
                           ORDER BY c.fecha_recepcion DESC, c.fecha_compra DESC, d.id DESC
                       ) AS posicion
                FROM detalle_compra d
                JOIN compra c ON c.id = d.compra_id
                WHERE c.estado = 'RECIBIDA' AND d.insumo_id IN (:insumoIds)
            ) recientes
            WHERE recientes.posicion = 1
            """, nativeQuery = true)
    List<Object[]> findUltimosCostosRecibidosByInsumos(
            @Param("insumoIds") Collection<Long> insumoIds);

    @Query("SELECT DISTINCT d.insumo.id FROM DetalleCompraModel d WHERE d.insumo.id IN :insumoIds")
    List<Long> findInsumoIdsConCompras(@Param("insumoIds") Collection<Long> insumoIds);
    
    @Query("SELECT d FROM DetalleCompraModel d WHERE d.compra.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    List<DetalleCompraModel> findByRangoFechas(
        @Param("fechaInicio") java.time.LocalDate fechaInicio,
        @Param("fechaFin") java.time.LocalDate fechaFin
    );

    /**
     * El orden por fecha e id descendentes es deliberado: el primer registro
     * de cada par insumo/proveedor representa la condición de compra más reciente.
     */
    @Query("""
            SELECT d
            FROM DetalleCompraModel d
            JOIN FETCH d.compra c
            JOIN FETCH c.proveedor p
            JOIN FETCH d.unidadCompra u
            WHERE d.insumo.id IN :insumoIds
              AND c.activo = true
              AND c.estado IN ('PENDIENTE', 'RECIBIDA_PARCIAL', 'RECIBIDA')
            ORDER BY c.fechaCompra DESC, d.id DESC
            """)
    List<DetalleCompraModel> findHistorialAbastecimiento(
            @Param("insumoIds") Collection<Long> insumoIds);

    /**
     * Incluye BORRADOR de forma intencional: una sugerencia ya convertida en
     * borrador debe descontarse para no proponer la misma compra nuevamente.
     */
    @Query("""
            SELECT d.insumo.id,
                   COALESCE(SUM(
                       (COALESCE(d.cantidad, 0) - COALESCE(d.cantidadRecibida, 0))
                       * COALESCE(d.factorConversion, 1)
                   ), 0)
            FROM DetalleCompraModel d
            JOIN d.compra c
            WHERE d.insumo.id IN :insumoIds
              AND c.activo = true
              AND c.estado IN ('BORRADOR', 'PENDIENTE', 'RECIBIDA_PARCIAL')
              AND COALESCE(d.cantidadRecibida, 0) < COALESCE(d.cantidad, 0)
            GROUP BY d.insumo.id
            """)
    List<Object[]> cantidadPendientePorInsumos(
            @Param("insumoIds") Collection<Long> insumoIds);
}
