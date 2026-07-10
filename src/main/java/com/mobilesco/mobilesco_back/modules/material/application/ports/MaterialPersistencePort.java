/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/material/application/ports/MaterialPersistencePort.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: MaterialPersistencePort
 * CONTEXTO: Puerto de salida de persistencia para desacoplar la aplicacion de JPA/repositorios concretos.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.material.application.ports;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;

public interface MaterialPersistencePort {

    MaterialModel save(MaterialModel material);

    List<MaterialModel> findAll();

    List<MaterialModel> findAll(Sort sort);

    Page<MaterialModel> findAll(Pageable pageable);

    Page<MaterialModel> buscarPaginado(Boolean activo, String busqueda, Pageable pageable);

    List<MaterialModel> findByActivo(Boolean activo);

    Optional<MaterialModel> findById(Long id);

    Optional<MaterialModel> findByCodigo(String codigo);

    Optional<MaterialModel> findByNombre(String nombre);

    boolean existsById(Long id);

    void deleteById(Long id);
}




