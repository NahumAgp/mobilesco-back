/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/application/ports/ColorPersistencePort.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ColorPersistencePort
 * CONTEXTO: Puerto de salida de persistencia para desacoplar la aplicacion de JPA/repositorios concretos.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.color.application.ports;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;

public interface ColorPersistencePort {

    ColorModel save(ColorModel color);

    List<ColorModel> findAll();

    List<ColorModel> findByActivo(Boolean activo);

    Page<ColorModel> buscarPaginado(Boolean activo, String busqueda, Pageable pageable);

    Optional<ColorModel> findById(Long id);

    Optional<ColorModel> findByNombre(String nombre);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    boolean existsById(Long id);

    void deleteById(Long id);
}




