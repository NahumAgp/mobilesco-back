/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/application/ports/ModeloFamiliaValidationPort.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ModeloFamiliaValidationPort
 * CONTEXTO: Puerto de salida para validaciones o consultas cruzadas entre modulos.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.familia.application.ports;

public interface ModeloFamiliaValidationPort {

    boolean existsByFamiliaId(Long familiaId);
}




