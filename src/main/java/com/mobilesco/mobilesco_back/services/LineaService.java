// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/LineaService.java
// ============================================
package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.linea.LineaCreateDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaResponseDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.LineaModel;
import com.mobilesco.mobilesco_back.repositories.LineaRepository;

@Service
public class LineaService {

    private final LineaRepository lineaRepository;

    public LineaService(LineaRepository lineaRepository) {
        this.lineaRepository = lineaRepository;
    }

    // ========== MAPPER ==========

    private LineaResponseDTO mapToResponseDTO(LineaModel linea) {
        LineaResponseDTO dto = new LineaResponseDTO();
        dto.setId(linea.getId());
        dto.setCodigo(linea.getCodigo());
        dto.setNombre(linea.getNombre());
        dto.setDescripcion(linea.getDescripcion());
        dto.setOrden(linea.getOrden());
        dto.setActivo(linea.getActivo());
        dto.setCreatedAt(linea.getCreatedAt());
        return dto;
    }

    private List<LineaResponseDTO> mapToResponseDTOList(List<LineaModel> lineas) {
        return lineas.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== CREATE ==========

    public LineaResponseDTO crear(LineaCreateDTO dto) {

        if (lineaRepository.existsByCodigo(dto.getCodigo())) {
            throw new BadRequestException("Ya existe una linea con el codigo: " + dto.getCodigo());
        }

        if (lineaRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe una linea con el nombre: " + dto.getNombre());
        }

        LineaModel linea = new LineaModel();
        linea.setCodigo(dto.getCodigo());
        linea.setNombre(dto.getNombre());
        linea.setDescripcion(dto.getDescripcion());
        linea.setOrden(Objects.requireNonNullElse(dto.getOrden(), 0));
        linea.setActivo(true);

        LineaModel guardado = lineaRepository.save(linea);
        return mapToResponseDTO(guardado);
    }

    // ========== READ ==========

    public List<LineaResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(lineaRepository.findAll());
    }

    public List<LineaResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(lineaRepository.findByActivo(true));
    }

    public LineaResponseDTO obtenerPorId(Long id) {
        LineaModel linea = lineaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Linea no encontrada con ID: " + id));
        return mapToResponseDTO(linea);
    }

    // ========== UPDATE ==========

    public LineaResponseDTO actualizar(Long id, LineaUpdateDTO dto) {

        LineaModel existente = lineaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Linea no encontrada con ID: " + id));

        if (dto.getCodigo() != null && !dto.getCodigo().equals(existente.getCodigo())) {
            if (lineaRepository.existsByCodigo(dto.getCodigo())) {
                throw new BadRequestException("Ya existe una linea con el codigo: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo());
        }

        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (lineaRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe una linea con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre());
        }

        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }

        if (dto.getOrden() != null) {
            existente.setOrden(dto.getOrden());
        }

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        LineaModel actualizado = lineaRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }

    // ========== DELETE ==========

    public void eliminar(Long id) {
        if (!lineaRepository.existsById(id)) {
            throw new NotFoundException("Linea no encontrada con ID: " + id);
        }
        lineaRepository.deleteById(id);
    }
}
