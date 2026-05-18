/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/categoria/application/ports/CategoriaPersistencePort.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: CategoriaPersistencePort
 * CONTEXTO: Puerto de salida de persistencia para desacoplar la aplicacion de JPA/repositorios concretos.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.categoria.application.ports;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;

import com.mobilesco.mobilesco_back.modules.categoria.domain.models.CategoriaModel;

public interface CategoriaPersistencePort {

    CategoriaModel save(CategoriaModel categoria);

    Optional<CategoriaModel> findById(Long id);

    List<CategoriaModel> findAll();

    List<CategoriaModel> findAll(Sort sort);

    List<CategoriaModel> findByActivoTrue();

    boolean existsByNombreIgnoreCase(String nombre);

    List<CategoriaModel> buscarPorNombre(String nombre);
}




