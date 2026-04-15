package com.mobilesco.mobilesco_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.DetalleCompraModel;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompraModel, Long> {
    
    List<DetalleCompraModel> findByCompraId(Long compraId);
    
    List<DetalleCompraModel> findByInsumoId(Long insumoId);
    
    @Query("SELECT d FROM DetalleCompraModel d WHERE d.insumo.id = :insumoId ORDER BY d.compra.fechaCompra DESC")
    List<DetalleCompraModel> findUltimasComprasByInsumo(@Param("insumoId") Long insumoId);
    
    @Query("SELECT d FROM DetalleCompraModel d WHERE d.compra.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    List<DetalleCompraModel> findByRangoFechas(
        @Param("fechaInicio") java.time.LocalDate fechaInicio,
        @Param("fechaFin") java.time.LocalDate fechaFin
    );
}