/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/infrastructure/out/persistence/repositories/FamiliaRepository.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: FamiliaRepository
 * CONTEXTO: Repositorio JPA del modulo Familia para consultas por linea y estado.
 * NOTAS: Mantener metodos de validacion de integridad por codigo/nombre/linea.
 */
package com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;

public interface FamiliaRepository extends JpaRepository<FamiliaModel, Long> {
    
    Optional<FamiliaModel> findByCodigo(String codigo);
    
    Optional<FamiliaModel> findByNombre(String nombre);
    
    List<FamiliaModel> findByActivo(Boolean activo);
    
    List<FamiliaModel> findByLineaId(Long lineaId);

    boolean existsByLineaId(Long lineaId);
    
    List<FamiliaModel> findByLineaIdAndActivo(Long lineaId, Boolean activo);
    
    boolean existsByCodigo(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByLineaIdAndCodigoIgnoreCase(Long lineaId, String codigo);

    boolean existsByLineaIdAndCodigoIgnoreCaseAndIdNot(Long lineaId, String codigo, Long id);
    
    boolean existsByNombre(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    boolean existsByLineaIdAndNombreIgnoreCase(Long lineaId, String nombre);

    boolean existsByLineaIdAndNombreIgnoreCaseAndIdNot(Long lineaId, String nombre, Long id);

    @Query("""
            SELECT f FROM FamiliaModel f
            LEFT JOIN f.linea l
            WHERE (:activo IS NULL OR f.activo = :activo)
              AND (:lineaId IS NULL OR l.id = :lineaId)
              AND (
                :busqueda IS NULL
                OR LOWER(f.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(f.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(f.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(l.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
              )
            """)
    Page<FamiliaModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            @Param("lineaId") Long lineaId,
            Pageable pageable);
}
