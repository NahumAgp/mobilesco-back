/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/material/infrastructure/out/persistence/repositories/MaterialRepository.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: MaterialRepository
 * CONTEXTO: Repositorio JPA del modulo Material para persistencia y validaciones.
 * NOTAS: Mantener metodos de existencia por codigo y nombre.
 */
package com.mobilesco.mobilesco_back.modules.material.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;

public interface MaterialRepository extends JpaRepository<MaterialModel, Long> {

    Optional<MaterialModel> findByCodigo(String codigo);

    Optional<MaterialModel> findByNombre(String nombre);

    List<MaterialModel> findByActivo(Boolean activo);

    boolean existsByCodigo(String codigo);

    boolean existsByNombre(String nombre);

    @Query("""
            SELECT m FROM MaterialModel m
            WHERE (:activo IS NULL OR m.activo = :activo)
              AND (
                :busqueda IS NULL
                OR LOWER(m.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(m.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
              )
            """)
    Page<MaterialModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
