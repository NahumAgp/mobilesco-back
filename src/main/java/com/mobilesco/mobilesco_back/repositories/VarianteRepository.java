// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/VarianteRepository.java
// ============================================
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.VarianteModel;

@Repository
public interface VarianteRepository extends JpaRepository<VarianteModel, Long> {

    Optional<VarianteModel> findBySku(String sku);

    List<VarianteModel> findByProductoBaseId(Long productoBaseId);

    boolean existsBySku(String sku);

    @Query("SELECT v FROM VarianteModel v WHERE " +
           "(:sku IS NULL OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:nombre IS NULL OR LOWER(v.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:productoBaseId IS NULL OR v.productoBase.id = :productoBaseId) AND " +
           "(:nivelId IS NULL OR v.nivel.id = :nivelId) AND " +
           "(:colorId IS NULL OR v.color.id = :colorId)")
    List<VarianteModel> buscarConFiltros(
            @Param("sku") String sku,
            @Param("nombre") String nombre,
            @Param("productoBaseId") Long productoBaseId,
            @Param("nivelId") Long nivelId,
            @Param("colorId") Long colorId
    );
}
