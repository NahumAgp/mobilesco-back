/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/infrastructure/out/persistence/repositories/ColorRepository.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ColorRepository
 * CONTEXTO: Repositorio JPA del modulo Color para operaciones CRUD y validaciones.
 * NOTAS: Mantener reglas de existencia por codigo y nombre.
 */
package com.mobilesco.mobilesco_back.modules.color.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;

@Repository
public interface ColorRepository extends JpaRepository<ColorModel, Long> {
    
    Optional<ColorModel> findByCodigo(String codigo);
    
    Optional<ColorModel> findByNombre(String nombre);
    
    List<ColorModel> findByActivo(Boolean activo);
    
    boolean existsByCodigo(String codigo);
    
    boolean existsByNombre(String nombre);
}
