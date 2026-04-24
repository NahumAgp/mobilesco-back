// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/FamiliaRepository.java
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.FamiliaModel;

@Repository
public interface FamiliaRepository extends JpaRepository<FamiliaModel, Long> {
    
    Optional<FamiliaModel> findByCodigo(String codigo);
    
    Optional<FamiliaModel> findByNombre(String nombre);
    
    List<FamiliaModel> findByActivo(Boolean activo);
    
    List<FamiliaModel> findByLineaId(Long lineaId);
    
    List<FamiliaModel> findByLineaIdAndActivo(Long lineaId, Boolean activo);
    
    boolean existsByCodigo(String codigo);
    
    boolean existsByNombre(String nombre);
}
