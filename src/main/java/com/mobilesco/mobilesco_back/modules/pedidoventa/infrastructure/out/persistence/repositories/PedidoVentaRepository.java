package com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.out.persistence.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.*;
import java.util.Collection;
import java.util.List;

public interface PedidoVentaRepository extends JpaRepository<PedidoVentaModel, Long> {
    boolean existsByCotizacionId(Long cotizacionId);

    @Query("select p.id from PedidoVentaModel p where p.estado in :estados")
    List<Long> buscarIdsPorEstados(@Param("estados") Collection<EstadoPedidoVenta> estados);

    @Query("""
        select distinct p from PedidoVentaModel p left join p.detalles d
        where (:estado is null or p.estado = :estado)
          and (:texto is null or lower(p.folio) like lower(concat('%', :texto, '%'))
               or lower(coalesce(p.cliente.nombre, '')) like lower(concat('%', :texto, '%'))
               or lower(d.skuSnapshot) like lower(concat('%', :texto, '%'))
               or lower(d.nombreSnapshot) like lower(concat('%', :texto, '%')))
        """)
    Page<PedidoVentaModel> buscar(@Param("estado") EstadoPedidoVenta estado,
            @Param("texto") String texto, Pageable pageable);
}
