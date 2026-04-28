package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.enums.TipoInsumo;
import com.mobilesco.mobilesco_back.models.ProveedorModel;

@Repository
public interface ProveedorRepository extends JpaRepository<ProveedorModel, Long> {

    List<ProveedorModel> findByActivo(Boolean activo);
    Page<ProveedorModel> findByActivo(Boolean activo, Pageable pageable);

    List<ProveedorModel> findByNombreContainingIgnoreCase(String nombre);
    Page<ProveedorModel> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);


    List<ProveedorModel> findByActivoAndNombreContainingIgnoreCase(Boolean activo, String nombre);
    Page<ProveedorModel> findByActivoAndNombreContainingIgnoreCase(Boolean activo, String nombre, Pageable pageable);

    Optional<ProveedorModel> findByRazonSocialIgnoreCase(String razonSocial);

    List<ProveedorModel> findByTipoInsumo(TipoInsumo tipoInsumo);

    @Query("""
        SELECT p
        FROM ProveedorModel p
        WHERE (:activo IS NULL OR p.activo = :activo)
          AND (:tipoInsumo IS NULL OR p.tipoInsumo = :tipoInsumo)
          AND (
                :busqueda IS NULL OR :busqueda = '' OR
                LOWER(COALESCE(p.razonSocial, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(p.rfc, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(
                    CONCAT(
                        COALESCE(p.nombre, ''),
                        CONCAT(
                            ' ',
                            CONCAT(
                                COALESCE(p.apellidoPaterno, ''),
                                CONCAT(' ', COALESCE(p.apellidoMaterno, ''))
                            )
                        )
                    )
                ) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(p.correo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(p.telefono, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
          )
        """)
    Page<ProveedorModel> buscarFiltradoPaginado(
            @Param("activo") Boolean activo,
            @Param("tipoInsumo") TipoInsumo tipoInsumo,
            @Param("busqueda") String busqueda,
            Pageable pageable);

    @Query("""
        SELECT p
        FROM ProveedorModel p
        WHERE (:activo IS NULL OR p.activo = :activo)
          AND (:tipoInsumo IS NULL OR p.tipoInsumo = :tipoInsumo)
          AND (
                :busqueda IS NULL OR :busqueda = '' OR
                LOWER(COALESCE(p.razonSocial, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(p.rfc, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(
                    CONCAT(
                        COALESCE(p.nombre, ''),
                        CONCAT(
                            ' ',
                            CONCAT(
                                COALESCE(p.apellidoPaterno, ''),
                                CONCAT(' ', COALESCE(p.apellidoMaterno, ''))
                            )
                        )
                    )
                ) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(p.correo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(p.telefono, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
          )
        """)
    List<ProveedorModel> buscarFiltrado(
            @Param("activo") Boolean activo,
            @Param("tipoInsumo") TipoInsumo tipoInsumo,
            @Param("busqueda") String busqueda);
     

     

    
    // (Opcional) si luego quieres activo+contacto:
    // List<ProveedorModel> findByActivoAndContactoContainingIgnoreCase(Boolean activo, String contacto);
}
