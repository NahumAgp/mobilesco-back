package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;

public interface RolRepository extends JpaRepository<RolModel, Long> {
    Optional<RolModel> findByName(String name);

    boolean existsByName(String name);

    @Override
    @EntityGraph(attributePaths = {"permisos"})
    java.util.List<RolModel> findAll();

    @EntityGraph(attributePaths = {"permisos"})
    @Query(
            value = """
                    select distinct r
                    from RolModel r
                    left join r.permisos p
                    where (:busqueda is null
                        or lower(r.name) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(r.descripcion, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(case when r.sistema = true then 'sistema' else 'personalizado' end) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(p.code, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(p.nombre, '')) like lower(concat('%', :busqueda, '%'))
                    )
                    """,
            countQuery = """
                    select count(distinct r)
                    from RolModel r
                    left join r.permisos p
                    where (:busqueda is null
                        or lower(r.name) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(r.descripcion, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(case when r.sistema = true then 'sistema' else 'personalizado' end) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(p.code, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(p.nombre, '')) like lower(concat('%', :busqueda, '%'))
                    )
                    """
    )
    Page<RolModel> buscarPaginado(@Param("busqueda") String busqueda, Pageable pageable);
}
