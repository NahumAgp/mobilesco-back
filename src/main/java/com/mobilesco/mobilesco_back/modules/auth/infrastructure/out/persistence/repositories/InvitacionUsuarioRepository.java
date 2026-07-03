package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.InvitacionUsuarioModel;

public interface InvitacionUsuarioRepository extends JpaRepository<InvitacionUsuarioModel, Long> {

    Optional<InvitacionUsuarioModel> findByToken(String token);

    Optional<InvitacionUsuarioModel> findByEmail(String email);

    boolean existsByToken(String token);

    boolean existsByEmail(String email);
}
