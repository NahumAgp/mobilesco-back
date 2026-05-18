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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialModel, Long> {

    Optional<MaterialModel> findByCodigo(String codigo);

    Optional<MaterialModel> findByNombre(String nombre);

    List<MaterialModel> findByActivo(Boolean activo);

    boolean existsByCodigo(String codigo);

    boolean existsByNombre(String nombre);
}
