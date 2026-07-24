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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;

public interface ModeloRepository extends JpaRepository<ModeloModel, Long> {

    Optional<ModeloModel> findByCodigo(String codigo);

    List<ModeloModel> findByFamiliaId(Long familiaId);

    List<ModeloModel> findByActivo(Boolean activo);

    boolean existsByFamiliaId(Long familiaId);

    boolean existsBySubfamiliaId(Long subfamiliaId);

    List<ModeloModel> findBySubfamiliaId(Long subfamiliaId);

    boolean existsByCodigo(String codigo);

    boolean existsByFamiliaIdAndCodigoIgnoreCase(Long familiaId, String codigo);

    boolean existsByFamiliaIdAndCodigoIgnoreCaseAndIdNot(Long familiaId, String codigo, Long id);

    boolean existsByFamiliaIdAndNombreIgnoreCase(Long familiaId, String nombre);

    boolean existsByFamiliaIdAndNombreIgnoreCaseAndIdNot(Long familiaId, String nombre, Long id);

    boolean existsBySubfamiliaIdAndCodigoIgnoreCase(Long subfamiliaId, String codigo);

    boolean existsBySubfamiliaIdAndCodigoIgnoreCaseAndIdNot(Long subfamiliaId, String codigo, Long id);

    boolean existsBySubfamiliaIdAndNombreIgnoreCase(Long subfamiliaId, String nombre);

    boolean existsBySubfamiliaIdAndNombreIgnoreCaseAndIdNot(Long subfamiliaId, String nombre, Long id);

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

    @Query("""
            SELECT m FROM ModeloModel m
            LEFT JOIN m.familia f
            LEFT JOIN m.subfamilia sf
            LEFT JOIN f.linea l
            WHERE (:activo IS NULL OR m.activo = :activo)
              AND (:familiaId IS NULL OR f.id = :familiaId)
              AND (
                :busqueda IS NULL
                OR LOWER(m.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(m.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(m.descripcionCorta, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(f.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(sf.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(l.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
              )
            """)
    Page<ModeloModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            @Param("familiaId") Long familiaId,
            Pageable pageable);
}
