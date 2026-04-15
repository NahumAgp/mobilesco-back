package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.ProductoModel;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoModel, Long> {
    
    Optional<ProductoModel> findBySku(String sku);
    
    List<ProductoModel> findByActivoTrue();
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductoModel p WHERE LOWER(p.sku) = LOWER(:sku)")
    boolean existsBySkuIgnoreCase(@Param("sku") String sku);
    
    @Query("SELECT p FROM ProductoModel p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<ProductoModel> buscarPorNombre(@Param("nombre") String nombre);
    
    List<ProductoModel> findByTipoProductoId(Long tipoProductoId);
    
    List<ProductoModel> findByLineaId(Long lineaId);
    
    List<ProductoModel> findByCategoriaId(Long categoriaId);
    
    List<ProductoModel> findByMaterialId(Long materialId);
}