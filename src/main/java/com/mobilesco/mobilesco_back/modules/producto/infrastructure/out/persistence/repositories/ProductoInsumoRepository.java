package com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;

public interface ProductoInsumoRepository extends JpaRepository<ProductoInsumoModel, Long> {
    
    List<ProductoInsumoModel> findByProductoId(Long productoId);

    Page<ProductoInsumoModel> findByProductoId(Long productoId, Pageable pageable);
    
    List<ProductoInsumoModel> findByInsumoId(Long insumoId);

    boolean existsByInsumoId(Long insumoId);

    boolean existsByProductoIdAndInsumoId(Long productoId, Long insumoId);
    
    void deleteByProductoId(Long productoId);
}
