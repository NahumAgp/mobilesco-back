package com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;

public interface TipoInsumoRepository extends JpaRepository<TipoInsumoModel, Long> {

    List<TipoInsumoModel> findAllByOrderByActivoDescNombreAsc();

    List<TipoInsumoModel> findByActivoTrueOrderByNombreAsc();

    Optional<TipoInsumoModel> findByCodigoIgnoreCase(String codigo);

    Optional<TipoInsumoModel> findByNombreNormalizado(String nombreNormalizado);

    @Query("""
            SELECT t
            FROM TipoInsumoModel t
            WHERE (:soloActivos = false OR t.activo = true)
              AND (
                    :busqueda IS NULL
                    OR LOWER(t.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(t.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
            """)
    Page<TipoInsumoModel> buscarPaginado(
            @Param("soloActivos") boolean soloActivos,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
