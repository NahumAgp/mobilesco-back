package com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedidaModel, Long> {

    List<UnidadMedidaModel> findByEstado(Boolean estado);

    @Query("""
            SELECT u FROM UnidadMedidaModel u
            WHERE (:estado IS NULL OR u.estado = :estado)
              AND (
                :busqueda IS NULL
                OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(u.simbolo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(u.tipo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
              )
            """)
    Page<UnidadMedidaModel> buscarPaginado(
            @Param("estado") Boolean estado,
            @Param("busqueda") String busqueda,
            Pageable pageable);
   
}
