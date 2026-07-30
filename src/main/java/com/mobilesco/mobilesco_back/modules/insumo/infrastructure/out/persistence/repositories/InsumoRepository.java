package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

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

    @Override
    @EntityGraph(attributePaths = "unidadMedida")
    Page<InsumoModel> findAll(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InsumoModel i WHERE i.id = :id")
    Optional<InsumoModel> findByIdForUpdate(@Param("id") Long id);
    
    // Buscar por nombre exacto
    Optional<InsumoModel> findByNombre(String nombre);
    
    // Verificar si existe por nombre (ignorando mayúsculas)
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM InsumoModel i WHERE LOWER(i.nombre) = LOWER(:nombre)")
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);
    
    // Listar activos
    List<InsumoModel> findByActivoTrue();
    
    // Buscar por nombre (para búsquedas)
    @Query("SELECT i FROM InsumoModel i WHERE LOWER(i.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND i.activo = true")
    List<InsumoModel> buscarPorNombre(@Param("nombre") String nombre);

    @Query("""
        SELECT i
        FROM InsumoModel i
        LEFT JOIN i.unidadMedida um
        WHERE (:activo IS NULL OR i.activo = :activo)
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
            WHERE (:activo IS NULL OR i.activo = :activo)
              AND (:stockBajo = false OR i.stockActual <= i.stockMinimo)
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
            WHERE :busqueda IS NULL OR :busqueda = ''
               OR LOWER(COALESCE(i.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
               OR LOWER(COALESCE(i.codigo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
               OR LOWER(COALESCE(i.codigoBarras, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
               OR LOWER(COALESCE(um.nombre, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
               OR LOWER(COALESCE(um.simbolo, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            """)
    Page<InsumoModel> buscarCostos(@Param("busqueda") String busqueda, Pageable pageable);
    
    // Filtrar por unidad de medida
    List<InsumoModel> findByUnidadMedidaId(Long unidadMedidaId);
    
    // Stock bajo (stock actual <= stock mínimo)
    @Query("SELECT i FROM InsumoModel i WHERE i.stockActual <= i.stockMinimo AND i.activo = true")
    List<InsumoModel> findWithStockBajo();

    @Query("""
            SELECT COUNT(i) FROM InsumoModel i
            WHERE i.activo = true AND i.stockMinimo IS NOT NULL AND i.stockActual <= i.stockMinimo
            """)
    long countWithStockBajo();
}
