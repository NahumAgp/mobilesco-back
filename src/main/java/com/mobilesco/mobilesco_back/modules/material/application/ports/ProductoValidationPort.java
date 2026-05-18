/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/material/application/ports/ProductoValidationPort.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoValidationPort
 * CONTEXTO: Puerto de salida para validaciones o consultas cruzadas entre modulos.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.material.application.ports;

public interface ProductoValidationPort {

    boolean existsByMaterialId(Long materialId);
}




