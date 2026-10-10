package com.mobilesco.mobilesco_back.modules.productoterminado.infrastructure.out.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mobilesco.mobilesco_back.modules.productoterminado.domain.models.*;

public interface MovimientoProductoTerminadoRepository extends JpaRepository<MovimientoProductoTerminadoModel, Long> {
    boolean existsByTipoAndReferenciaId(TipoMovimientoProductoTerminado tipo, Long referenciaId);
}
