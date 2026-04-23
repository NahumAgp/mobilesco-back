// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/EstiloRepository.java
// ============================================
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.EstiloModel;

@Repository
public interface EstiloRepository extends JpaRepository<EstiloModel, Long> {
    
    Optional<EstiloModel> findByNombre(String nombre);
    
    List<EstiloModel> findByActivo(Boolean activo);
    
    List<EstiloModel> findByFamiliaId(Long familiaId);
    
    List<EstiloModel> findByFamiliaIdAndActivo(Long familiaId, Boolean activo);
    
    boolean existsByNombre(String nombre);
}
