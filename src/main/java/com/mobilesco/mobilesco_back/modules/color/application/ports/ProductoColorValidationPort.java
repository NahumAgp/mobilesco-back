/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/application/ports/ProductoColorValidationPort.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoColorValidationPort
 * CONTEXTO: Puerto de salida para validaciones o consultas cruzadas entre modulos.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.color.application.ports;

public interface ProductoColorValidationPort {

    boolean existsByColorId(Long colorId);
}




