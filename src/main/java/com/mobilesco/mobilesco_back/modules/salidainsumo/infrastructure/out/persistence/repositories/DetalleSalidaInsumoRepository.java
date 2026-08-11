package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Collection;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.DetalleSalidaInsumoModel;

public interface DetalleSalidaInsumoRepository extends JpaRepository<DetalleSalidaInsumoModel, Long> {
    List<DetalleSalidaInsumoModel> findBySalidaInsumoIdOrderByIdAsc(Long salidaInsumoId);

    boolean existsByInsumoId(Long insumoId);

    @Query("SELECT DISTINCT d.insumo.id FROM DetalleSalidaInsumoModel d WHERE d.insumo.id IN :insumoIds")
    List<Long> findInsumoIdsConSalidas(@Param("insumoIds") Collection<Long> insumoIds);

    @Query("""
            SELECT COALESCE(SUM(d.costoTotal), 0)
            FROM DetalleSalidaInsumoModel d
            WHERE d.salidaInsumo.activo = true
              AND d.salidaInsumo.fechaSalida >= :desde
              AND d.salidaInsumo.fechaSalida < :hasta
            """)
    Double sumarConsumoValorizado(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
