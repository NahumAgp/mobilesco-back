package com.mobilesco.mobilesco_back.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.InvitacionUsuarioModel;

@Repository
public interface InvitacionUsuarioRepository extends JpaRepository<InvitacionUsuarioModel, Long> {

    Optional<InvitacionUsuarioModel> findByToken(String token);

    boolean existsByToken(String token);

    boolean existsByEmail(String email);
}
