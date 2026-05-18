/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/application/ports/LineaLookupPort.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaLookupPort
 * CONTEXTO: Puerto de salida para validaciones o consultas cruzadas entre modulos.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.familia.application.ports;

import java.util.Optional;

import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;

public interface LineaLookupPort {

    Optional<LineaModel> findById(Long id);

    boolean existsById(Long id);
}




