package com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.subfamilia.domain.models.SubfamiliaModel;

public interface SubfamiliaRepository extends JpaRepository<SubfamiliaModel, Long> {

    List<SubfamiliaModel> findByActivo(Boolean activo);

    List<SubfamiliaModel> findByFamiliaId(Long familiaId);

    List<SubfamiliaModel> findByFamiliaIdAndActivo(Long familiaId, Boolean activo);

    boolean existsByFamiliaId(Long familiaId);

    boolean existsByFamiliaIdAndCodigoIgnoreCase(Long familiaId, String codigo);

    boolean existsByFamiliaIdAndCodigoIgnoreCaseAndIdNot(Long familiaId, String codigo, Long id);

    boolean existsByFamiliaIdAndNombreIgnoreCase(Long familiaId, String nombre);

    boolean existsByFamiliaIdAndNombreIgnoreCaseAndIdNot(Long familiaId, String nombre, Long id);

    @Query("""
            SELECT s FROM SubfamiliaModel s
            LEFT JOIN s.familia f
            LEFT JOIN f.linea l
            WHERE (:activo IS NULL OR s.activo = :activo)
              AND (:familiaId IS NULL OR f.id = :familiaId)
              AND (
                :busqueda IS NULL
                OR LOWER(s.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(s.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(f.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(l.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
              )
            """)
    Page<SubfamiliaModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            @Param("familiaId") Long familiaId,
            Pageable pageable);
}
