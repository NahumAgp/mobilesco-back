package com.mobilesco.mobilesco_back.modules.kardex.infrastructure.out.persistence.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.kardex.domain.models.MovimientoInsumoModel;

public interface KardexRepository extends JpaRepository<MovimientoInsumoModel, Long> {
    
    // Historial de un insumo específico
    List<MovimientoInsumoModel> findByInsumoIdOrderByFechaDesc(Long insumoId);

    Page<MovimientoInsumoModel> findByInsumoId(Long insumoId, Pageable pageable);

    Page<MovimientoInsumoModel> findByInsumoIdAndFechaBetween(
        Long insumoId, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    // Movimientos por tipo
    List<MovimientoInsumoModel> findByInsumoIdAndTipoOrderByFechaDesc(Long insumoId, String tipo);
    
    // Movimientos por concepto
    List<MovimientoInsumoModel> findByInsumoIdAndConceptoOrderByFechaDesc(Long insumoId, String concepto);
    
    // Movimientos por rango de fechas
    List<MovimientoInsumoModel> findByInsumoIdAndFechaBetweenOrderByFechaDesc(
        Long insumoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Todos los movimientos de un período
    List<MovimientoInsumoModel> findByFechaBetweenOrderByFechaDesc(
        LocalDateTime fechaInicio, LocalDateTime fechaFin);

    Page<MovimientoInsumoModel> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    // Movimientos relacionados con una compra
    List<MovimientoInsumoModel> findByCompraIdOrderByFechaDescIdDesc(Long compraId);

    boolean existsByCompraId(Long compraId);

    boolean existsByInsumoId(Long insumoId);
    
    // Último movimiento de un insumo (para conocer el costo actual)
    @Query("SELECT m FROM MovimientoInsumoModel m WHERE m.insumo.id = :insumoId ORDER BY m.fecha DESC LIMIT 1")
    MovimientoInsumoModel findUltimoMovimientoByInsumo(@Param("insumoId") Long insumoId);
    
    // Costo promedio ponderado de un insumo
    @Query("SELECT SUM(m.costoTotal) / SUM(m.cantidad) FROM MovimientoInsumoModel m " +
           "WHERE m.insumo.id = :insumoId AND m.tipo = 'ENTRADA'")
    Double calcularCostoPromedio(@Param("insumoId") Long insumoId);

    @Query("""
            SELECT m.insumo.id, SUM(m.costoTotal) / SUM(m.cantidad)
            FROM MovimientoInsumoModel m
            WHERE m.insumo.id IN :insumoIds AND m.tipo = 'ENTRADA'
            GROUP BY m.insumo.id
            """)
    List<Object[]> calcularCostosPromedio(@Param("insumoIds") Collection<Long> insumoIds);

    @Query("SELECT DISTINCT m.insumo.id FROM MovimientoInsumoModel m WHERE m.insumo.id IN :insumoIds")
    List<Long> findInsumoIdsConMovimientos(@Param("insumoIds") Collection<Long> insumoIds);
    
    // Consumo total en un período
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoInsumoModel m " +
           "WHERE m.insumo.id = :insumoId AND m.tipo = 'SALIDA' " +
           "AND m.fecha BETWEEN :fechaInicio AND :fechaFin")
    Double consumoEnPeriodo(
        @Param("insumoId") Long insumoId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
}
