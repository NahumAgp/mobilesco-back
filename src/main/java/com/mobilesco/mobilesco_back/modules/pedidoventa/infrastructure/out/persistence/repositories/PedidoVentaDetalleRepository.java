package com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.out.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.PedidoVentaDetalleModel;

public interface PedidoVentaDetalleRepository extends JpaRepository<PedidoVentaDetalleModel, Long> {
}
