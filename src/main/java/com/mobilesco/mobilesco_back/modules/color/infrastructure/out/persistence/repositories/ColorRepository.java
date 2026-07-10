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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;

public interface ColorRepository extends JpaRepository<ColorModel, Long> {
    
    Optional<ColorModel> findByCodigo(String codigo);
    
    Optional<ColorModel> findByNombre(String nombre);
    
    List<ColorModel> findByActivo(Boolean activo);
    
    boolean existsByCodigo(String codigo);
    
    boolean existsByNombre(String nombre);

    @Query("""
            SELECT c
            FROM ColorModel c
            WHERE (:activo IS NULL OR c.activo = :activo)
              AND (
                    :busqueda IS NULL
                    OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.hex, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
            """)
    Page<ColorModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
