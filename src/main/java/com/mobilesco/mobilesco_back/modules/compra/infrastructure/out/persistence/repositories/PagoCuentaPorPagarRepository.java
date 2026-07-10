package com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.PagoCuentaPorPagarModel;

public interface PagoCuentaPorPagarRepository extends JpaRepository<PagoCuentaPorPagarModel, Long> {
    List<PagoCuentaPorPagarModel> findByCuentaPorPagarIdOrderByFechaPagoDescIdDesc(Long cuentaPorPagarId);
}
