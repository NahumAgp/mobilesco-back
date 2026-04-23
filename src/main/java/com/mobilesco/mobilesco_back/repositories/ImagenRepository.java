// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/ImagenRepository.java
// ============================================
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.models.ImagenModel;

@Repository
public interface ImagenRepository extends JpaRepository<ImagenModel, Long> {
    
    List<ImagenModel> findByVarianteId(Long varianteId);
    
    List<ImagenModel> findByVarianteIdOrderByOrdenAsc(Long varianteId);
    
    Optional<ImagenModel> findByVarianteIdAndEsPrincipalTrue(Long varianteId);
    
    @Modifying
    @Transactional
    @Query("UPDATE ImagenModel i SET i.esPrincipal = false WHERE i.variante.id = :varianteId")
    void resetPrincipalFlag(@Param("varianteId") Long varianteId);
    
    long countByVarianteId(Long varianteId);
}