// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/NivelRepository.java
package com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;

public interface NivelRepository extends JpaRepository<NivelModel, Long> {
    
    Optional<NivelModel> findByCodigo(String codigo);
    
    Optional<NivelModel> findByNombre(String nombre);
    
    List<NivelModel> findByActivo(Boolean activo);

    List<NivelModel> findByModeloIdOrderByNombreAsc(Long modeloId);

    List<NivelModel> findByModeloIdAndActivoTrueOrderByNombreAsc(Long modeloId);

    List<NivelModel> findByModeloIdAndActivoTrueOrderByCodigoAsc(Long modeloId);

    List<NivelModel> findByModeloIdOrderByCodigoAsc(Long modeloId);

    List<NivelModel> findByCategoriaId(Long categoriaId);

    Optional<NivelModel> findByModeloIdAndCategoriaId(Long modeloId, Long categoriaId);

    long countByModeloId(Long modeloId);

    long countByModeloIdAndActivoTrue(Long modeloId);

    void deleteByModeloId(Long modeloId);
    
    boolean existsByCodigo(String codigo);
    
    boolean existsByNombre(String nombre);

    boolean existsByModeloIdAndCodigoIgnoreCase(Long modeloId, String codigo);

    boolean existsByModeloIdAndNombreIgnoreCase(Long modeloId, String nombre);

    boolean existsByModeloIdAndCategoriaId(Long modeloId, Long categoriaId);

    boolean existsByModeloIdAndCodigoIgnoreCaseAndIdNot(Long modeloId, String codigo, Long id);

    boolean existsByModeloIdAndNombreIgnoreCaseAndIdNot(Long modeloId, String nombre, Long id);

    @Query("""
            SELECT n
            FROM NivelModel n
            LEFT JOIN n.modelo m
            WHERE (:activo IS NULL OR n.activo = :activo)
              AND (
                    :busqueda IS NULL
                    OR LOWER(n.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(n.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(n.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(m.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
            """)
    Page<NivelModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
