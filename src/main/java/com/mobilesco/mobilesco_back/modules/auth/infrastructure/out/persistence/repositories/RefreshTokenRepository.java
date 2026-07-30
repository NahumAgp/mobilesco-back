package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.RefreshTokenModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, Long> {

    Optional<RefreshTokenModel> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from RefreshTokenModel rt where rt.tokenHash = :tokenHash")
    Optional<RefreshTokenModel> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<RefreshTokenModel> findByUser(UsuarioModel user);

    long deleteByExpiresAtBefore(LocalDateTime now);

}
