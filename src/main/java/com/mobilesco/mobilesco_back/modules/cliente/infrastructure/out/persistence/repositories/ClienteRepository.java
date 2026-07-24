package com.mobilesco.mobilesco_back.modules.cliente.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClasificacionCliente;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;

public interface ClienteRepository extends JpaRepository<ClienteModel, Long> {

    Optional<ClienteModel> findByRfcIgnoreCase(String rfc);

    List<ClienteModel> findByActivoTrueOrderByNombreAscRazonSocialAsc();

    @Query("""
            SELECT c FROM ClienteModel c
            WHERE (:activo IS NULL OR c.activo = :activo)
              AND (:clasificacion IS NULL OR c.clasificacion = :clasificacion)
              AND (
                    :busqueda IS NULL
                    OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.razonSocial, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.nombreComercial, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.rfc, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.correo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.telefono, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
              )
            """)
    Page<ClienteModel> buscar(
            @Param("activo") Boolean activo,
            @Param("clasificacion") ClasificacionCliente clasificacion,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
