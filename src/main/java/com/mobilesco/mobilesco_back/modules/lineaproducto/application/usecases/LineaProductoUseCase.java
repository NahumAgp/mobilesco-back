/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/lineaproducto/application/usecases/LineaProductoUseCase.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaProductoUseCase
 * CONTEXTO: Contrato de casos de uso del modulo en la capa de aplicacion.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.lineaproducto.application.usecases;

import java.util.List;

import com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.dtos.LineaProductoCreateDTO;
import com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.dtos.LineaProductoResponseDTO;
import com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.dtos.LineaProductoUpdateDTO;

public interface LineaProductoUseCase {

    LineaProductoResponseDTO crear(LineaProductoCreateDTO dto);

    LineaProductoResponseDTO actualizar(Long id, LineaProductoUpdateDTO dto);

    LineaProductoResponseDTO obtenerPorId(Long id);

    List<LineaProductoResponseDTO> listar();

    List<LineaProductoResponseDTO> listarActivos();

    List<LineaProductoResponseDTO> buscar(String nombre);

    void eliminar(Long id);
}




