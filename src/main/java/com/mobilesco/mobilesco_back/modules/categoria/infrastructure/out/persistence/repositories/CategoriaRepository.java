/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/categoria/infrastructure/out/persistence/repositories/CategoriaRepository.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: CategoriaRepository
 * CONTEXTO: Repositorio JPA del modulo Categoria para operaciones de persistencia.
 * NOTAS: Mantener queries orientadas a filtros de estado y busqueda por nombre.
 */
package com.mobilesco.mobilesco_back.modules.categoria.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.categoria.domain.models.CategoriaModel;

public interface CategoriaRepository extends JpaRepository<CategoriaModel, Long> {
    
    Optional<CategoriaModel> findByNombre(String nombre);
    
    List<CategoriaModel> findByActivoTrue();
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CategoriaModel c WHERE LOWER(c.nombre) = LOWER(:nombre)")
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT c FROM CategoriaModel c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.activo = true")
    List<CategoriaModel> buscarPorNombre(@Param("nombre") String nombre);
}
