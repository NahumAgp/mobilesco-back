/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/lineaproducto/application/ports/LineaProductoPersistencePort.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaProductoPersistencePort
 * CONTEXTO: Puerto de salida de persistencia para desacoplar la aplicacion de JPA/repositorios concretos.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.lineaproducto.application.ports;

import java.util.List;
import java.util.Optional;

import com.mobilesco.mobilesco_back.modules.lineaproducto.domain.models.LineaProductoModel;

public interface LineaProductoPersistencePort {

    LineaProductoModel save(LineaProductoModel lineaProducto);

    Optional<LineaProductoModel> findById(Long id);

    List<LineaProductoModel> findAll();

    List<LineaProductoModel> findByActivoTrue();

    List<LineaProductoModel> buscarPorNombre(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}




