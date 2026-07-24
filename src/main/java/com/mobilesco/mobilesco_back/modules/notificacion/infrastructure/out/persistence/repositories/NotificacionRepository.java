package com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.NotificacionModel;

public interface NotificacionRepository extends JpaRepository<NotificacionModel, Long> {

    @Query("""
            SELECT n FROM NotificacionModel n
            WHERE n.destinatario.id = :usuarioId
              AND (:leida IS NULL OR n.leida = :leida)
            """)
    Page<NotificacionModel> listarPropias(
            @Param("usuarioId") Long usuarioId,
            @Param("leida") Boolean leida,
            Pageable pageable);

    long countByDestinatarioIdAndLeidaFalse(Long usuarioId);

    Optional<NotificacionModel> findByIdAndDestinatarioId(Long id, Long usuarioId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE NotificacionModel n
            SET n.leida = true, n.fechaLectura = CURRENT_TIMESTAMP
            WHERE n.destinatario.id = :usuarioId AND n.leida = false
            """)
    int marcarTodasLeidas(@Param("usuarioId") Long usuarioId);
}
