package com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;

@Repository
public interface ProductoInsumoRepository extends JpaRepository<ProductoInsumoModel, Long> {
    
    List<ProductoInsumoModel> findByProductoId(Long productoId);
    
    List<ProductoInsumoModel> findByInsumoId(Long insumoId);
    
    void deleteByProductoId(Long productoId);
}