/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/lineaproducto/infrastructure/out/persistence/repositories/LineaProductoRepository.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaProductoRepository
 * CONTEXTO: Repositorio JPA del modulo LineaProducto con busqueda y validaciones.
 * NOTAS: Preservar queries case-insensitive sobre nombre.
 */
package com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.modules.lineaproducto.domain.models.LineaProductoModel;

@Repository
public interface LineaProductoRepository extends JpaRepository<LineaProductoModel, Long> {
    
    Optional<LineaProductoModel> findByNombre(String nombre);
    
    List<LineaProductoModel> findByActivoTrue();
    
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LineaProductoModel l WHERE LOWER(l.nombre) = LOWER(:nombre)")
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT l FROM LineaProductoModel l WHERE LOWER(l.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND l.activo = true")
    List<LineaProductoModel> buscarPorNombre(@Param("nombre") String nombre);
}
