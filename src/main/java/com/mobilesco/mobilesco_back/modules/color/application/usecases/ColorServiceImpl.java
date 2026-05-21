/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/application/usecases/ColorServiceImpl.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ColorServiceImpl
 * CONTEXTO: Implementacion de casos de uso del modulo; orquesta reglas de negocio y puertos de salida.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.color.application.usecases;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorCreateDTO;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorResponseDTO;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.color.application.ports.ColorPersistencePort;
import com.mobilesco.mobilesco_back.modules.color.application.ports.ProductoColorValidationPort;

@Service
public class ColorServiceImpl implements ColorUseCase {

    private final ColorPersistencePort colorRepository;
    private final ProductoColorValidationPort productoRepository;

    public ColorServiceImpl(ColorPersistencePort colorRepository, ProductoColorValidationPort productoRepository) {
        this.colorRepository = colorRepository;
        this.productoRepository = productoRepository;
    }

    // ========== MAPPER ==========

    private ColorResponseDTO mapToResponseDTO(ColorModel color) {
        ColorResponseDTO dto = new ColorResponseDTO();
        dto.setId(color.getId());
        dto.setCodigo(color.getCodigo());
        dto.setNombre(color.getNombre());
        dto.setDescripcion(color.getDescripcion());
        dto.setHex(color.getHex());
        dto.setActivo(color.getActivo());
        dto.setCreatedAt(color.getCreatedAt());
        return dto;
    }

    private List<ColorResponseDTO> mapToResponseDTOList(List<ColorModel> colores) {
        return colores.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== CREATE ==========

    public ColorResponseDTO crear(ColorCreateDTO dto) {

        if (colorRepository.existsByCodigo(dto.getCodigo())) {
            throw new BadRequestException("Ya existe un color con el codigo: " + dto.getCodigo());
        }

        if (colorRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe un color con el nombre: " + dto.getNombre());
        }

        ColorModel color = new ColorModel();
        color.setCodigo(dto.getCodigo());
        color.setNombre(dto.getNombre());
        color.setDescripcion(dto.getDescripcion());
        color.setHex(dto.getHex());
        color.setActivo(true);

        ColorModel guardado = colorRepository.save(color);
        return mapToResponseDTO(guardado);
    }

    // ========== READ ==========

    public List<ColorResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(colorRepository.findAll());
    }

    public List<ColorResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(colorRepository.findByActivo(true));
    }

    public ColorResponseDTO obtenerPorId(Long id) {
        ColorModel color = colorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Color no encontrado con ID: " + id));
        return mapToResponseDTO(color);
    }

    public ColorResponseDTO obtenerPorNombre(String nombre) {
        ColorModel color = colorRepository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Color no encontrado con nombre: " + nombre));
        return mapToResponseDTO(color);
    }

    // ========== UPDATE ==========

    public ColorResponseDTO actualizar(Long id, ColorUpdateDTO dto) {

        ColorModel existente = colorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Color no encontrado con ID: " + id));

        if (dto.getCodigo() != null && !dto.getCodigo().equals(existente.getCodigo())) {
            if (colorRepository.existsByCodigo(dto.getCodigo())) {
                throw new BadRequestException("Ya existe un color con el codigo: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo());
        }

        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (colorRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe un color con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre());
        }

        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }

        if (dto.getHex() != null) {
            existente.setHex(dto.getHex());
        }

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        ColorModel actualizado = colorRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }

    public ColorResponseDTO activar(Long id) {
        ColorModel existente = colorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Color no encontrado con ID: " + id));

        existente.setActivo(true);
        return mapToResponseDTO(colorRepository.save(existente));
    }

    public ColorResponseDTO desactivar(Long id) {
        ColorModel existente = colorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Color no encontrado con ID: " + id));

        existente.setActivo(false);
        return mapToResponseDTO(colorRepository.save(existente));
    }

    // ========== DELETE ==========

    public void eliminar(Long id) {
        if (!colorRepository.existsById(id)) {
            throw new NotFoundException("Color no encontrado con ID: " + id);
        }

        if (productoRepository.existsByColorId(id)) {
            throw new BadRequestException("No se puede eliminar el color porque tiene productos asociados");
        }

        colorRepository.deleteById(id);
    }
}





