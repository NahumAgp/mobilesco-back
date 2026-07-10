/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/application/usecases/FamiliaUseCase.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: FamiliaUseCase
 * CONTEXTO: Contrato de casos de uso del modulo en la capa de aplicacion.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.familia.application.usecases;

import java.util.List;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaCreateDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaUpdateDTO;

public interface FamiliaUseCase {

    FamiliaResponseDTO crear(FamiliaCreateDTO dto);

    String sugerirCodigo(String nombre);

    String sugerirCodigo(String nombre, Long lineaId);

    byte[] generarReporteExcel(Boolean activo, String busqueda, Long lineaId, String sortBy, String direction);

    List<FamiliaResponseDTO> obtenerTodos();

    PageResponseDTO<FamiliaResponseDTO> obtenerPaginado(
            int page,
            String sortBy,
            String direction,
            Boolean activo,
            String busqueda,
            Long lineaId);

    List<FamiliaResponseDTO> obtenerActivos();

    FamiliaResponseDTO obtenerPorId(Long id);

    List<FamiliaResponseDTO> obtenerPorLinea(Long lineaId);

    List<FamiliaResponseDTO> obtenerPorLineaYActivo(Long lineaId, Boolean activo);

    FamiliaResponseDTO actualizar(Long id, FamiliaUpdateDTO dto);

    FamiliaResponseDTO activar(Long id);

    FamiliaResponseDTO desactivar(Long id);

    void eliminar(Long id);
}




