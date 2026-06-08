/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/application/usecases/ColorUseCase.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ColorUseCase
 * CONTEXTO: Contrato de casos de uso del modulo en la capa de aplicacion.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.color.application.usecases;

import java.util.List;

import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorCreateDTO;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorResponseDTO;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorUpdateDTO;

public interface ColorUseCase {

    ColorResponseDTO crear(ColorCreateDTO dto);

    String sugerirCodigo(String hex);

    List<ColorResponseDTO> obtenerTodos();

    List<ColorResponseDTO> obtenerActivos();

    ColorResponseDTO obtenerPorId(Long id);

    ColorResponseDTO obtenerPorNombre(String nombre);

    ColorResponseDTO actualizar(Long id, ColorUpdateDTO dto);

    ColorResponseDTO activar(Long id);

    ColorResponseDTO desactivar(Long id);

    void eliminar(Long id);
}




