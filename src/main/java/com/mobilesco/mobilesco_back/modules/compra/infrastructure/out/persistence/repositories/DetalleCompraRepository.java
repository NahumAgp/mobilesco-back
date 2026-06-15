package com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories;

import java.util.List;

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
    
    @Query("SELECT d FROM DetalleCompraModel d WHERE d.compra.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    List<DetalleCompraModel> findByRangoFechas(
        @Param("fechaInicio") java.time.LocalDate fechaInicio,
        @Param("fechaFin") java.time.LocalDate fechaFin
    );
}
