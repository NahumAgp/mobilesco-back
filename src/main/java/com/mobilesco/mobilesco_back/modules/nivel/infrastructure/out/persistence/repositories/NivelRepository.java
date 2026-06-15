// RUTA: src/main/java/com/mobilesco/mobilesco_back/repositories/NivelRepository.java
package com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;

public interface NivelRepository extends JpaRepository<NivelModel, Long> {
    
    Optional<NivelModel> findByCodigo(String codigo);
    
    Optional<NivelModel> findByNombre(String nombre);
    
    List<NivelModel> findByActivo(Boolean activo);

    List<NivelModel> findByModeloIdOrderByNombreAsc(Long modeloId);

    List<NivelModel> findByModeloIdAndActivoTrueOrderByNombreAsc(Long modeloId);

    long countByModeloId(Long modeloId);

    long countByModeloIdAndActivoTrue(Long modeloId);

    void deleteByModeloId(Long modeloId);
    
    boolean existsByCodigo(String codigo);
    
    boolean existsByNombre(String nombre);

    boolean existsByModeloIdAndCodigoIgnoreCase(Long modeloId, String codigo);

    boolean existsByModeloIdAndNombreIgnoreCase(Long modeloId, String nombre);

    boolean existsByModeloIdAndCodigoIgnoreCaseAndIdNot(Long modeloId, String codigo, Long id);

    boolean existsByModeloIdAndNombreIgnoreCaseAndIdNot(Long modeloId, String nombre, Long id);
}
