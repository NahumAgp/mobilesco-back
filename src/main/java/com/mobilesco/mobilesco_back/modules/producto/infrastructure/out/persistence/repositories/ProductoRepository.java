/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/out/persistence/repositories/ProductoRepository.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoRepository
 * CONTEXTO: Repositorio JPA del modulo Producto.
 * NOTAS: Incluye consultas por SKU, filtros y relaciones de catalogo.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;

public interface ProductoRepository extends JpaRepository<ProductoModel, Long> {
    
    Optional<ProductoModel> findBySku(String sku);
    
    List<ProductoModel> findByActivoTrue();
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductoModel p WHERE LOWER(p.sku) = LOWER(:sku)")
    boolean existsBySkuIgnoreCase(@Param("sku") String sku);
    
    @Query("SELECT p FROM ProductoModel p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<ProductoModel> buscarPorNombre(@Param("nombre") String nombre);
        
    List<ProductoModel> findByLineaId(Long lineaId);
    
    List<ProductoModel> findByModeloId(Long modeloId);

    List<ProductoModel> findByModeloIdAndNivelId(Long modeloId, Long nivelId);

    boolean existsByModeloId(Long modeloId);

    boolean existsByNivelId(Long nivelId);

    boolean existsByColorId(Long colorId);

    boolean existsByMaterialId(Long materialId);

    @Query("SELECT p FROM ProductoModel p WHERE " +
           "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:modeloId IS NULL OR p.modelo.id = :modeloId) AND " +
           "(:nivelId IS NULL OR p.nivel.id = :nivelId) AND " +
           "(:colorId IS NULL OR p.color.id = :colorId) AND " +
           "(:materialId IS NULL OR p.material.id = :materialId)")
    List<ProductoModel> buscarConFiltros(
            @Param("sku") String sku,
            @Param("nombre") String nombre,
            @Param("modeloId") Long modeloId,
            @Param("nivelId") Long nivelId,
            @Param("colorId") Long colorId,
            @Param("materialId") Long materialId
    );

    @Query("""
            SELECT p
            FROM ProductoModel p
            LEFT JOIN p.modelo m
            LEFT JOIN m.familia f
            LEFT JOIN f.linea l
            LEFT JOIN p.nivel n
            LEFT JOIN p.material mat
            LEFT JOIN p.color c
            WHERE (m IS NOT NULL OR n IS NOT NULL OR mat IS NOT NULL OR c IS NOT NULL)
              AND (:activo IS NULL OR p.activo = :activo)
              AND (:modeloId IS NULL OR m.id = :modeloId)
              AND (:nivelId IS NULL OR n.id = :nivelId)
              AND (:colorId IS NULL OR c.id = :colorId)
              AND (
                    :busqueda IS NULL
                    OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(p.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(p.descripcionCorta, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(m.codigo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(m.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(f.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(l.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(n.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(mat.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
            """)
    Page<ProductoModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            @Param("modeloId") Long modeloId,
            @Param("nivelId") Long nivelId,
            @Param("colorId") Long colorId,
            Pageable pageable);
}
