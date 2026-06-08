/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/material/application/usecases/MaterialUseCase.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: MaterialUseCase
 * CONTEXTO: Contrato de casos de uso del modulo en la capa de aplicacion.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.material.application.usecases;

import java.util.List;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialCreateDTO;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialUpdateDTO;

public interface MaterialUseCase {

    MaterialResponseDTO crear(MaterialCreateDTO dto);

    String sugerirCodigo(String nombre);

    List<MaterialResponseDTO> obtenerTodos();

    PageResponseDTO<MaterialResponseDTO> obtenerPaginado(int page, String sortBy, String direction);

    byte[] generarReporteExcel(Boolean activo, String busqueda, String sortBy, String direction);

    List<MaterialResponseDTO> obtenerActivos();

    MaterialResponseDTO obtenerPorId(Long id);

    MaterialResponseDTO actualizar(Long id, MaterialUpdateDTO dto);

    MaterialResponseDTO activar(Long id);

    MaterialResponseDTO desactivar(Long id);

    void eliminar(Long id);
}




