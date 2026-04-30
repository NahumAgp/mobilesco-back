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
        
    List<ProductoModel> findByLineaId(Long lineaId);
    
    List<ProductoModel> findByModeloId(Long modeloId);

    @Query("SELECT p FROM ProductoModel p WHERE " +
           "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:modeloId IS NULL OR p.modelo.id = :modeloId) AND " +
           "(:nivelId IS NULL OR p.nivel.id = :nivelId) AND " +
           "(:colorId IS NULL OR p.color.id = :colorId)")
    List<ProductoModel> buscarConFiltros(
            @Param("sku") String sku,
            @Param("nombre") String nombre,
            @Param("modeloId") Long modeloId,
            @Param("nivelId") Long nivelId,
            @Param("colorId") Long colorId
    );
}
