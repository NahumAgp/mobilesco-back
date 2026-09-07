package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;

import jakarta.persistence.LockModeType;

public interface InsumoRepository extends JpaRepository<InsumoModel, Long> {

    @EntityGraph(attributePaths = {"unidadMedida", "componentes.insumo.unidadMedida"})
    List<InsumoModel> findByConjuntoTrueOrderByNombreAsc();

    @Query("SELECT COUNT(c) > 0 FROM ComponenteInsumoModel c WHERE c.insumo.id = :id")
    boolean esComponente(@Param("id") Long id);

    @Override
    @Query("SELECT i FROM InsumoModel i WHERE i.conjunto = false")
    List<InsumoModel> findAll();

    @Override
    @Query("SELECT i FROM InsumoModel i WHERE i.conjunto = false")
    List<InsumoModel> findAll(org.springframework.data.domain.Sort sort);

    @Override
    @EntityGraph(attributePaths = "unidadMedida")
    @Query("SELECT i FROM InsumoModel i WHERE i.conjunto = false")
    Page<InsumoModel> findAll(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InsumoModel i WHERE i.id = :id")
    Optional<InsumoModel> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM InsumoModel i
            JOIN FETCH i.unidadMedida
            WHERE i.id IN :ids
            ORDER BY i.id ASC
            """)
    List<InsumoModel> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
    
    // Buscar por nombre exacto
    Optional<InsumoModel> findByNombre(String nombre);
    
    // Verificar si existe por nombre (ignorando mayúsculas)
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM InsumoModel i WHERE LOWER(i.nombre) = LOWER(:nombre)")
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);
    
    // Listar activos
    @Query("SELECT i FROM InsumoModel i WHERE i.activo = true AND i.conjunto = false")
    List<InsumoModel> findByActivoTrue();
    
    // Buscar por nombre (para búsquedas)
    @Query("SELECT i FROM InsumoModel i WHERE LOWER(i.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND i.activo = true AND i.conjunto = false")
    List<InsumoModel> buscarPorNombre(@Param("nombre") String nombre);

    @Query("""
        SELECT i
        FROM InsumoModel i
        LEFT JOIN i.unidadMedida um
        WHERE i.conjunto = false AND (:activo IS NULL OR i.activo = :activo)
          AND (
                :busqueda IS NULL OR :busqueda = '' OR
                LOWER(COALESCE(i.codigo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(i.codigoBarras, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(i.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(i.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(i.ubicacion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(i.fila, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(i.columna, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(um.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(COALESCE(um.simbolo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
          )
        ORDER BY i.nombre ASC, i.id ASC
        """)
    List<InsumoModel> buscarPorTermino(
            @Param("busqueda") String busqueda,
            @Param("activo") Boolean activo);

    @EntityGraph(attributePaths = "unidadMedida")
    @Query("""
            SELECT i
            FROM InsumoModel i
            LEFT JOIN i.unidadMedida um
            WHERE i.conjunto = false AND (:activo IS NULL OR i.activo = :activo)
              AND (:stockBajo = false OR (i.stockActual - COALESCE(i.stockApartado, 0)) <= i.stockMinimo)
              AND (
                    :busqueda IS NULL OR :busqueda = '' OR
                    LOWER(CAST(i.id AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(COALESCE(i.codigo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(COALESCE(i.codigoBarras, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(COALESCE(i.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(COALESCE(i.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(CAST(i.tipoInsumo AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(COALESCE(i.ubicacion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(COALESCE(i.fila, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(COALESCE(i.columna, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(COALESCE(um.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(COALESCE(um.simbolo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(CAST(i.costoCotizacion AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(CASE WHEN i.activo = true THEN 'activo' ELSE 'inactivo' END)
                        LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                    LOWER(CAST(i.fechaRegistro AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
            """)
    Page<InsumoModel> buscarPaginado(
            @Param("busqueda") String busqueda,
            @Param("activo") Boolean activo,
            @Param("stockBajo") boolean stockBajo,
            Pageable pageable);

    @Query("""
            SELECT i FROM InsumoModel i
            LEFT JOIN i.unidadMedida um
            WHERE i.conjunto = false AND (:busqueda IS NULL OR :busqueda = ''
               OR LOWER(COALESCE(i.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
               OR LOWER(COALESCE(i.codigo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
               OR LOWER(COALESCE(i.codigoBarras, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
               OR LOWER(COALESCE(um.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
               OR LOWER(COALESCE(um.simbolo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<InsumoModel> buscarCostos(@Param("busqueda") String busqueda, Pageable pageable);
    
    // Filtrar por unidad de medida
    @Query("SELECT i FROM InsumoModel i WHERE i.unidadMedida.id = :unidadMedidaId AND i.conjunto = false")
    List<InsumoModel> findByUnidadMedidaId(@Param("unidadMedidaId") Long unidadMedidaId);
    
    // Stock bajo (stock actual <= stock mínimo)
    @Query("SELECT i FROM InsumoModel i WHERE (i.stockActual - COALESCE(i.stockApartado, 0)) <= i.stockMinimo AND i.activo = true AND i.conjunto = false")
    List<InsumoModel> findWithStockBajo();

    @Query("""
            SELECT COUNT(i) FROM InsumoModel i
            WHERE i.conjunto = false AND i.activo = true AND i.stockMinimo IS NOT NULL AND (i.stockActual - COALESCE(i.stockApartado, 0)) <= i.stockMinimo
            """)
    long countWithStockBajo();

    /**
     * Valoriza la existencia con el mejor costo disponible: promedio de entradas,
     * ultimo costo de compra recibido y, como respaldo, costo de cotizacion.
     */
    @Query(value = """
            SELECT COALESCE(SUM(
                GREATEST(COALESCE(i.stock_actual, 0), 0) *
                COALESCE(k.costo_promedio, c.ultimo_costo, i.costo_cotizar, 0)
            ), 0)
            FROM insumo i
            LEFT JOIN (
                SELECT insumo_id, SUM(costo_total) / NULLIF(SUM(cantidad), 0) AS costo_promedio
                FROM kardex
                WHERE tipo = 'ENTRADA'
                GROUP BY insumo_id
            ) k ON k.insumo_id = i.id
            LEFT JOIN (
                SELECT costos.insumo_id, costos.costo AS ultimo_costo
                FROM (
                    SELECT d.insumo_id,
                           d.precio_unitario / NULLIF(d.factor_conversion, 0) AS costo,
                           ROW_NUMBER() OVER (
                               PARTITION BY d.insumo_id
                               ORDER BY cp.fecha_recepcion DESC, cp.fecha_compra DESC, d.id DESC
                           ) AS posicion
                    FROM detalle_compra d
                    JOIN compra cp ON cp.id = d.compra_id
                    WHERE cp.estado = 'RECIBIDA'
                ) costos
                WHERE costos.posicion = 1
            ) c ON c.insumo_id = i.id
            WHERE i.activo = true
            """, nativeQuery = true)
    Double calcularValorTotalInventario();
}
