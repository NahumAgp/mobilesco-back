/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/modelo/infrastructure/out/persistence/repositories/ModeloRepository.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ModeloRepository
 * CONTEXTO: Repositorio JPA del modulo Modelo.
 * NOTAS: Incluye consultas filtradas por familia y busquedas dinamicas.
 */
package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;

public interface ModeloRepository extends JpaRepository<ModeloModel, Long> {

    Optional<ModeloModel> findByCodigo(String codigo);

    List<ModeloModel> findByFamiliaId(Long familiaId);

    List<ModeloModel> findByActivo(Boolean activo);

    boolean existsByFamiliaId(Long familiaId);

    boolean existsByCodigo(String codigo);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM ModeloModel m JOIN m.materiales material WHERE material.id = :materialId")
    boolean existsByMaterialesId(@Param("materialId") Long materialId);

    @Query("SELECT p FROM ModeloModel p WHERE " +
           "(:codigo IS NULL OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :codigo, '%'))) AND " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:familiaId IS NULL OR p.familia.id = :familiaId)")
    List<ModeloModel> buscarConFiltros(
            @Param("codigo") String codigo,
            @Param("nombre") String nombre,
            @Param("familiaId") Long familiaId
    );
}
