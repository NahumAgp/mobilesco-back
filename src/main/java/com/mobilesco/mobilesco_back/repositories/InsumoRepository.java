package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.InsumoModel;

@Repository
public interface InsumoRepository extends JpaRepository<InsumoModel, Long> {
    
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
    
    // Filtrar por unidad de medida
    List<InsumoModel> findByUnidadMedidaId(Long unidadMedidaId);
    
    // Stock bajo (stock actual <= stock mínimo)
    @Query("SELECT i FROM InsumoModel i WHERE i.stockActual <= i.stockMinimo AND i.activo = true")
    List<InsumoModel> findWithStockBajo();
}