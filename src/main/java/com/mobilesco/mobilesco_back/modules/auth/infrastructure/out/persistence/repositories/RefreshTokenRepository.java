package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.RefreshTokenModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, Long> {

    Optional<RefreshTokenModel> findByTokenHash(String tokenHash);

    List<RefreshTokenModel> findByUser(UsuarioModel user);

    long deleteByExpiresAtBefore(LocalDateTime now);

}
