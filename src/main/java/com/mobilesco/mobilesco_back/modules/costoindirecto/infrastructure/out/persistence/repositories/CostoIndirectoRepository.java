package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.TipoCostoIndirecto;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.models.CostoIndirectoModel;

public interface CostoIndirectoRepository extends JpaRepository<CostoIndirectoModel, Long> {
    
    Optional<CostoIndirectoModel> findByCodigo(String codigo);
    
    boolean existsByCodigoIgnoreCase(String codigo);
    
    List<CostoIndirectoModel> findByActivoTrue();
    long countByActivoTrue();
    
    List<CostoIndirectoModel> findByTipo(TipoCostoIndirecto tipo);
    
    List<CostoIndirectoModel> findByBaseDistribucion(BaseDistribucion base);
    
    @Query("SELECT c FROM CostoIndirectoModel c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<CostoIndirectoModel> buscarPorNombre(@Param("nombre") String nombre);

    @Query("""
            SELECT c
            FROM CostoIndirectoModel c
            WHERE (:activo IS NULL OR c.activo = :activo)
              AND (
                    :busqueda IS NULL
                    OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(c.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(CAST(c.tipo AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(CAST(c.periodicidad AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
            """)
    Page<CostoIndirectoModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
