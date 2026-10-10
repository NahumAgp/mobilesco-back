package com.mobilesco.mobilesco_back.modules.productoterminado.infrastructure.out.persistence.repositories;

import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.mobilesco.mobilesco_back.modules.productoterminado.domain.models.InventarioProductoTerminadoModel;
import jakarta.persistence.LockModeType;

public interface InventarioProductoTerminadoRepository extends JpaRepository<InventarioProductoTerminadoModel, Long> {
    Optional<InventarioProductoTerminadoModel> findByProductoId(Long productoId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventarioProductoTerminadoModel i where i.producto.id = :productoId")
    Optional<InventarioProductoTerminadoModel> findByProductoIdForUpdate(@Param("productoId") Long productoId);
    @Query("""
        select i from InventarioProductoTerminadoModel i
        where (:texto is null or lower(i.producto.sku) like lower(concat('%', :texto, '%'))
             or lower(i.producto.nombre) like lower(concat('%', :texto, '%')))
        """)
    Page<InventarioProductoTerminadoModel> buscar(@Param("texto") String texto, Pageable pageable);
}
