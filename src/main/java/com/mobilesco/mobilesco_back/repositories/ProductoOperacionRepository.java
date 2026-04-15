package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.ProductoOperacionModel;

@Repository
public interface ProductoOperacionRepository extends JpaRepository<ProductoOperacionModel, Long> {
    
    List<ProductoOperacionModel> findByProductoIdOrderByOrdenAsc(Long productoId);
    
    Optional<ProductoOperacionModel> findByProductoIdAndOperacionId(Long productoId, Long operacionId);
    
    @Modifying
    @Query("DELETE FROM ProductoOperacionModel po WHERE po.producto.id = :productoId AND po.operacion.id = :operacionId")
    void deleteByProductoIdAndOperacionId(@Param("productoId") Long productoId, @Param("operacionId") Long operacionId);
    
    boolean existsByProductoIdAndOperacionId(Long productoId, Long operacionId);
    
    @Query("SELECT SUM(po.tiempoTotal) FROM ProductoOperacionModel po WHERE po.producto.id = :productoId")
    Double sumarTiempoTotalByProducto(@Param("productoId") Long productoId);
    
    @Query("SELECT SUM(po.importeActividad) FROM ProductoOperacionModel po WHERE po.producto.id = :productoId")
    Double sumarCostoTotalByProducto(@Param("productoId") Long productoId);
}