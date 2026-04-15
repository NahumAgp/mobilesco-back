package com.mobilesco.mobilesco_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.FamiliaModel;

@Repository
public interface FamiliaRepository extends JpaRepository<FamiliaModel, Long> {

    // Para validación en creación
    boolean existsByNombreAndPadreIsNull(String nombre);
    
    boolean existsByNombreAndPadreId(String nombre, Long padreId);
    
    // 🔴 NUEVOS - Para validación en actualización (excluyendo el ID actual)
    boolean existsByNombreAndPadreIsNullAndIdNot(String nombre, Long id);
    
    boolean existsByNombreAndPadreIdAndIdNot(String nombre, Long padreId, Long id);
    
    // Para verificar hijos
    List<FamiliaModel> findByPadreIdAndActivoTrue(Long padreId);
    
    // Para listar activas
    List<FamiliaModel> findByActivoTrue();
    
    // Para buscar raíces
    List<FamiliaModel> findByPadreIsNullAndActivoTrue();
    
    // Para búsquedas
    List<FamiliaModel> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
}