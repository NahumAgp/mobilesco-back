package com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;

public interface ProductoInsumoRepository extends JpaRepository<ProductoInsumoModel, Long> {
    
    List<ProductoInsumoModel> findByProductoId(Long productoId);

    Page<ProductoInsumoModel> findByProductoId(Long productoId, Pageable pageable);
    
    List<ProductoInsumoModel> findByInsumoId(Long insumoId);

    boolean existsByInsumoId(Long insumoId);

    boolean existsByProductoIdAndInsumoId(Long productoId, Long insumoId);

    @Query("SELECT DISTINCT p.insumo.id FROM ProductoInsumoModel p WHERE p.insumo.id IN :insumoIds")
    List<Long> findInsumoIdsConProductos(@Param("insumoIds") Collection<Long> insumoIds);
    
    void deleteByProductoId(Long productoId);
}
