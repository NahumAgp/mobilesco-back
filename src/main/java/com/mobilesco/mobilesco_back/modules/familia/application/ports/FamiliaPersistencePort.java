/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/application/ports/FamiliaPersistencePort.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: FamiliaPersistencePort
 * CONTEXTO: Puerto de salida de persistencia para desacoplar la aplicacion de JPA/repositorios concretos.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.familia.application.ports;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;

public interface FamiliaPersistencePort {

    Optional<FamiliaModel> findById(Long id);

    List<FamiliaModel> findAll();

    List<FamiliaModel> findAll(Sort sort);

    Page<FamiliaModel> findAll(Pageable pageable);

    List<FamiliaModel> findByActivo(Boolean activo);

    List<FamiliaModel> findByLineaId(Long lineaId);

    List<FamiliaModel> findByLineaIdAndActivo(Long lineaId, Boolean activo);

    boolean existsById(Long id);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByNombre(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    FamiliaModel save(FamiliaModel familia);

    void deleteById(Long id);
}




