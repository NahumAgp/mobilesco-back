package com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;

public interface DestinatarioNotificacionRepository extends Repository<UsuarioModel, Long> {

    @Query("""
            SELECT DISTINCT u FROM UsuarioModel u
            JOIN u.roles r
            WHERE r.name IN :roles
              AND u.enabled = true
              AND u.locked = false
            """)
    List<UsuarioModel> buscarActivosPorRoles(@Param("roles") Set<String> roles);
}
