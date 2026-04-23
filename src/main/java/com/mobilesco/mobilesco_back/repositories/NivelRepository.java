// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/NivelRepository.java
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.NivelModel;

@Repository
public interface NivelRepository extends JpaRepository<NivelModel, Long> {
    
    Optional<NivelModel> findByCodigo(String codigo);
    
    Optional<NivelModel> findByNombre(String nombre);
    
    List<NivelModel> findByActivo(Boolean activo);
    
    boolean existsByCodigo(String codigo);
    
    boolean existsByNombre(String nombre);
}