package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;

public interface RolRepository extends JpaRepository<RolModel, Long> {
    Optional<RolModel> findByName(String name);
}