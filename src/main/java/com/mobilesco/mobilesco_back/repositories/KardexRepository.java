package com.mobilesco.mobilesco_back.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.MovimientoInsumoModel;

@Repository
public interface KardexRepository extends JpaRepository<MovimientoInsumoModel, Long> {
    
    // Historial de un insumo específico
    List<MovimientoInsumoModel> findByInsumoIdOrderByFechaDesc(Long insumoId);
    
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
    
    // Movimientos relacionados con una compra
    List<MovimientoInsumoModel> findByCompraId(Long compraId);
    
    // Último movimiento de un insumo (para conocer el costo actual)
    @Query("SELECT m FROM MovimientoInsumoModel m WHERE m.insumo.id = :insumoId ORDER BY m.fecha DESC LIMIT 1")
    MovimientoInsumoModel findUltimoMovimientoByInsumo(@Param("insumoId") Long insumoId);
    
    // Costo promedio ponderado de un insumo
    @Query("SELECT SUM(m.costoTotal) / SUM(m.cantidad) FROM MovimientoInsumoModel m " +
           "WHERE m.insumo.id = :insumoId AND m.tipo = 'ENTRADA'")
    Double calcularCostoPromedio(@Param("insumoId") Long insumoId);
    
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