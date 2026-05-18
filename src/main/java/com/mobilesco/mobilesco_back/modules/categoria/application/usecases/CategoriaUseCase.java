/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/categoria/application/usecases/CategoriaUseCase.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: CategoriaUseCase
 * CONTEXTO: Contrato de casos de uso del modulo en la capa de aplicacion.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.categoria.application.usecases;

import java.util.List;

import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.in.api.dtos.CategoriaCreateDTO;
import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.in.api.dtos.CategoriaResponseDTO;
import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.in.api.dtos.CategoriaUpdateDTO;

public interface CategoriaUseCase {

    CategoriaResponseDTO crear(CategoriaCreateDTO dto);

    CategoriaResponseDTO actualizar(Long id, CategoriaUpdateDTO dto);

    CategoriaResponseDTO obtenerPorId(Long id);

    List<CategoriaResponseDTO> listar();

    byte[] generarReporteExcel(Boolean activo, String busqueda, String sortBy, String direction);

    List<CategoriaResponseDTO> listarActivos();

    List<CategoriaResponseDTO> buscar(String nombre);

    CategoriaResponseDTO activar(Long id);

    CategoriaResponseDTO desactivar(Long id);

    void eliminar(Long id);
}




