package com.mobilesco.mobilesco_back.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.ProduccionModel;

@Repository
public interface ProduccionRepository extends JpaRepository<ProduccionModel, Long> {

    List<ProduccionModel> findByProductoId(Long productoId);

    List<ProduccionModel> findByEstado(String estado);

    List<ProduccionModel> findByFechaProduccionBetween(LocalDate inicio, LocalDate fin);

    @Query("SELECT COUNT(p) FROM ProduccionModel p WHERE p.producto.id = :productoId AND p.fechaProduccion BETWEEN :inicio AND :fin")
    Long countByProductoAndPeriodo(
            @Param("productoId") Long productoId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(p.cantidad), 0) FROM ProduccionModel p WHERE p.estado = 'TERMINADA' AND p.fechaProduccion BETWEEN :inicio AND :fin")
    Long totalUnidadesEnPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(pt.minutosReales), 0) FROM ProduccionTiempoModel pt " +
           "WHERE pt.produccion.fechaProduccion BETWEEN :inicio AND :fin")
    Double totalHorasModEnPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(pt.minutosReales), 0) FROM ProduccionTiempoModel pt " +
           "WHERE pt.centroTrabajo.id = :centroId AND pt.produccion.fechaProduccion BETWEEN :inicio AND :fin")
    Double totalHorasMaquinaByCentro(
            @Param("centroId") Long centroId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(pt.minutosReales), 0) FROM ProduccionTiempoModel pt " +
           "WHERE pt.produccion.fechaProduccion BETWEEN :inicio AND :fin")
    Double totalHorasMaquinaEnPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    boolean existsByFolio(String folio);

    List<ProduccionModel> findByFechaProduccion(LocalDate fecha);

    @Query("SELECT p FROM ProduccionModel p WHERE p.fechaInicio BETWEEN :inicio AND :fin OR p.fechaFin BETWEEN :inicio AND :fin")
    List<ProduccionModel> findEnRangoFechas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}