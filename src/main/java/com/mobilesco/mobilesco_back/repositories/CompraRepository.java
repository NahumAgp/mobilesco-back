package com.mobilesco.mobilesco_back.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.CompraModel;

@Repository
public interface CompraRepository extends JpaRepository<CompraModel, Long> {
    
    List<CompraModel> findByProveedorId(Long proveedorId);
    
    List<CompraModel> findByEstado(String estado);
    
    @Query("SELECT c FROM CompraModel c WHERE c.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    List<CompraModel> findByRangoFechas(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    @Query("SELECT c FROM CompraModel c WHERE c.proveedor.id = :proveedorId AND c.estado = :estado")
    List<CompraModel> findByProveedorAndEstado(
        @Param("proveedorId") Long proveedorId,
        @Param("estado") String estado
    );
    
    @Query("SELECT c FROM CompraModel c WHERE c.folio LIKE %:folio%")
    List<CompraModel> buscarPorFolio(@Param("folio") String folio);
    
    boolean existsByNumeroDocumento(String numeroDocumento);

    
}