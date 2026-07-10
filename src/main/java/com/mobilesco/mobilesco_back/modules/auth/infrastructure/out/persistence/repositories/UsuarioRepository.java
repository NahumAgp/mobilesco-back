package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.EstadoCuentaUsuario;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✅ este es el que usa /me, y debe regresar UsuarioModel, NO Object
    @EntityGraph(attributePaths = {"roles", "roles.permisos", "permisos", "empleado", "invitacion"})
    Optional<UsuarioModel> findOneByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"roles", "roles.permisos", "permisos", "empleado", "invitacion"})
    java.util.List<UsuarioModel> findAll();

    Optional<UsuarioModel> findByEmpleado(EmpleadoModel empleado);

    @EntityGraph(attributePaths = {"roles", "roles.permisos", "permisos", "empleado", "invitacion"})
    @Query(
            value = """
                    select distinct u
                    from UsuarioModel u
                    left join u.empleado e
                    left join u.roles r
                    where (:busqueda is null
                        or lower(u.email) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(e.nombre, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(e.apellidoPaterno, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(e.apellidoMaterno, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(r.name, '')) like lower(concat('%', :busqueda, '%'))
                    )
                    """,
            countQuery = """
                    select count(distinct u)
                    from UsuarioModel u
                    left join u.empleado e
                    left join u.roles r
                    where (:busqueda is null
                        or lower(u.email) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(e.nombre, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(e.apellidoPaterno, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(e.apellidoMaterno, '')) like lower(concat('%', :busqueda, '%'))
                        or lower(coalesce(r.name, '')) like lower(concat('%', :busqueda, '%'))
                    )
                    """
    )
    Page<UsuarioModel> buscarPaginado(@Param("busqueda") String busqueda, Pageable pageable);

    @EntityGraph(attributePaths = {"roles", "roles.permisos", "permisos", "empleado", "invitacion"})
    @Query(
            value = """
                    select u
                    from UsuarioModel u
                    where (u.estadoCuenta is not null and u.estadoCuenta <> :estadoActivo)
                       or (u.estadoCuenta is null and (u.enabled = false or u.locked = true))
                    """,
            countQuery = """
                    select count(u)
                    from UsuarioModel u
                    where (u.estadoCuenta is not null and u.estadoCuenta <> :estadoActivo)
                       or (u.estadoCuenta is null and (u.enabled = false or u.locked = true))
                    """
    )
    Page<UsuarioModel> buscarPendientes(@Param("estadoActivo") EstadoCuentaUsuario estadoActivo, Pageable pageable);
}
