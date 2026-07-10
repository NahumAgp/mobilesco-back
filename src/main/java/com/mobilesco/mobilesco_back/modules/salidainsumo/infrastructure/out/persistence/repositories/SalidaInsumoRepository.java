package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories;

import java.util.List;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.SalidaInsumoModel;

public interface SalidaInsumoRepository extends JpaRepository<SalidaInsumoModel, Long> {
    List<SalidaInsumoModel> findAllByOrderByFechaSalidaDesc();
    List<SalidaInsumoModel> findByActivoTrueOrderByFechaSalidaDesc();

    @Query("""
            SELECT s
            FROM SalidaInsumoModel s
            WHERE s.activo = true
              AND (:area IS NULL OR s.area = :area)
              AND (:responsable IS NULL OR s.responsable = :responsable)
              AND (:fechaInicio IS NULL OR s.fechaSalida >= :fechaInicio)
              AND (:fechaFin IS NULL OR s.fechaSalida <= :fechaFin)
              AND (
                    :busqueda IS NULL
                    OR LOWER(COALESCE(s.tipoSalida, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(s.ordenProduccion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(s.responsable, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(s.area, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(s.usuario, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(s.observaciones, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR EXISTS (
                        SELECT d.id
                        FROM DetalleSalidaInsumoModel d
                        WHERE d.salidaInsumo = s
                          AND LOWER(d.insumo.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    )
                  )
            """)
    Page<SalidaInsumoModel> buscarPaginado(
            @Param("busqueda") String busqueda,
            @Param("area") String area,
            @Param("responsable") String responsable,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);
}
