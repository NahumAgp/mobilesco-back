// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/TipoRepository.java
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.TipoModel;

@Repository
public interface TipoRepository extends JpaRepository<TipoModel, Long> {
    
    Optional<TipoModel> findByNombre(String nombre);
    
    List<TipoModel> findByActivo(Boolean activo);
    
    boolean existsByNombre(String nombre);
}