// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/LineaRepository.java
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.LineaModel;

@Repository
public interface LineaRepository extends JpaRepository<LineaModel, Long> {
    
    Optional<LineaModel> findByCodigo(String codigo);

    Optional<LineaModel> findByNombre(String nombre);
    
    List<LineaModel> findByActivo(Boolean activo);
    
    boolean existsByCodigo(String codigo);
    
    boolean existsByNombre(String nombre);
}
