package com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.out.persistence.repositories;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.*;
import java.time.LocalDate;

public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccionModel,Long> {
    boolean existsByCotizacionId(Long cotizacionId);
    @Query("""
        select distinct o from OrdenProduccionModel o left join o.detalles d
        where (:estado is null or o.estado=:estado) and (:origen is null or o.origen=:origen)
        and (:desde is null or o.fechaInicioProgramada>=:desde) and (:hasta is null or o.fechaInicioProgramada<=:hasta)
        and (:texto is null or lower(o.folio) like lower(concat('%',:texto,'%'))
             or lower(d.nombreSnapshot) like lower(concat('%',:texto,'%'))
             or lower(d.skuSnapshot) like lower(concat('%',:texto,'%'))
             or lower(coalesce(o.cliente.nombre,'')) like lower(concat('%',:texto,'%')))
        """)
    Page<OrdenProduccionModel> buscar(@Param("estado") EstadoOrdenProduccion estado, @Param("origen") OrigenOrdenProduccion origen,
        @Param("texto") String texto, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, Pageable pageable);
}
