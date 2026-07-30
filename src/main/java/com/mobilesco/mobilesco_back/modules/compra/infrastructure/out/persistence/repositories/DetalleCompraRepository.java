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
}
