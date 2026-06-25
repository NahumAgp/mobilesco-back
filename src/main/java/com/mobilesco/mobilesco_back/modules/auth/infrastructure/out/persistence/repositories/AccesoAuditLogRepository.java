package com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.AccesoAuditLogModel;

public interface AccesoAuditLogRepository extends JpaRepository<AccesoAuditLogModel, Long> {
}
