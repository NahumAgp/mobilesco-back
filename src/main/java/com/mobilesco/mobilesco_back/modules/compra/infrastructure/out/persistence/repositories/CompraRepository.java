package com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;

public interface CompraRepository extends JpaRepository<CompraModel, Long> {

    List<CompraModel> findByActivoTrue();
    
    List<CompraModel> findByProveedorIdAndActivoTrue(Long proveedorId);
    
    List<CompraModel> findByEstadoAndActivoTrue(String estado);
    
    @Query("SELECT c FROM CompraModel c WHERE c.activo = true AND c.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    List<CompraModel> findByRangoFechas(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    @Query("SELECT c FROM CompraModel c WHERE c.activo = true AND c.proveedor.id = :proveedorId AND c.estado = :estado")
    List<CompraModel> findByProveedorAndEstado(
        @Param("proveedorId") Long proveedorId,
        @Param("estado") String estado
    );
    
    @Query("SELECT c FROM CompraModel c WHERE c.activo = true AND c.folio LIKE %:folio%")
    List<CompraModel> buscarPorFolio(@Param("folio") String folio);

    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByProveedorId(Long proveedorId);

    
}
