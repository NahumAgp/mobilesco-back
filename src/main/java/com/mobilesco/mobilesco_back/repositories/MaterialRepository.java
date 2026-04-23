// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/MaterialRepository.java
// ============================================
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.MaterialModel;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialModel, Long> {
    
    Optional<MaterialModel> findByNombre(String nombre);
    
    List<MaterialModel> findByActivo(Boolean activo);
    
    boolean existsByNombre(String nombre);
}