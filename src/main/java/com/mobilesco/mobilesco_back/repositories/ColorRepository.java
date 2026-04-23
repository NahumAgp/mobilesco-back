// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/ColorRepository.java
package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.ColorModel;

@Repository
public interface ColorRepository extends JpaRepository<ColorModel, Long> {
    
    Optional<ColorModel> findByNombre(String nombre);
    
    List<ColorModel> findByActivo(Boolean activo);
    
    boolean existsByNombre(String nombre);
}