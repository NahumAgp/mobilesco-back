// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/ProductoBaseRepository.java
// ============================================
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.ProductoBaseModel;

@Repository
public interface ProductoBaseRepository extends JpaRepository<ProductoBaseModel, Long> {

    Optional<ProductoBaseModel> findBySku(String sku);

    List<ProductoBaseModel> findByFamiliaId(Long familiaId);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM ProductoBaseModel p WHERE " +
           "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:familiaId IS NULL OR p.familia.id = :familiaId)")
    List<ProductoBaseModel> buscarConFiltros(
            @Param("sku") String sku,
            @Param("nombre") String nombre,
            @Param("familiaId") Long familiaId
    );
}
