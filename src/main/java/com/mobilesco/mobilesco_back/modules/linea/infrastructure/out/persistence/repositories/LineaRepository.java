/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/linea/infrastructure/out/persistence/repositories/LineaRepository.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaRepository
 * CONTEXTO: Repositorio JPA del modulo Linea.
 * NOTAS: Soporta consultas por codigo, nombre y estado activo.
 */
package com.mobilesco.mobilesco_back.modules.linea.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;

@Repository
public interface LineaRepository extends JpaRepository<LineaModel, Long> {
    
    Optional<LineaModel> findByCodigo(String codigo);

    Optional<LineaModel> findByNombre(String nombre);
    
    List<LineaModel> findByActivo(Boolean activo);
    
    boolean existsByCodigo(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
    
    boolean existsByNombre(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    @Query("SELECT l.codigo FROM LineaModel l")
    List<String> findAllCodigos();
}
