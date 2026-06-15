// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/ImagenRepository.java
// ============================================
package com.mobilesco.mobilesco_back.modules.imagen.infrastructure.out.persistence.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.imagen.domain.models.ImagenModel;

public interface ImagenRepository extends JpaRepository<ImagenModel, Long> {
    
    List<ImagenModel> findByProductoId(Long productoId);
    
    List<ImagenModel> findByProductoIdOrderByOrdenAsc(Long productoId);
    
    Optional<ImagenModel> findByProductoIdAndEsPrincipalTrue(Long productoId);

    @Modifying
    @Transactional
    @Query("UPDATE ImagenModel i SET i.esPrincipal = false WHERE i.producto.id = :productoId")
    void resetPrincipalFlag(@Param("productoId") Long productoId);
    
    long countByProductoId(Long productoId);
}
