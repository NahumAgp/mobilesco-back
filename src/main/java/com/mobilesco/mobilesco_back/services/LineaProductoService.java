package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.LineaProducto.LineaProductoCreateDTO;
import com.mobilesco.mobilesco_back.dto.LineaProducto.LineaProductoResponseDTO;
import com.mobilesco.mobilesco_back.dto.LineaProducto.LineaProductoUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.LineaProductoModel;
import com.mobilesco.mobilesco_back.repositories.LineaProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LineaProductoService {

    private final LineaProductoRepository lineaProductoRepository;

    @Transactional
    public LineaProductoResponseDTO crear(LineaProductoCreateDTO dto) {
        // Validar nombre único
        if (lineaProductoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe una línea de producto con el nombre: " + dto.getNombre());
        }

        // Crear entidad
        LineaProductoModel linea = LineaProductoModel.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();

        LineaProductoModel saved = lineaProductoRepository.save(linea);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public LineaProductoResponseDTO actualizar(Long id, LineaProductoUpdateDTO dto) {
        LineaProductoModel linea = lineaProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Línea de producto no encontrada con id: " + id));

        // Validar nombre único (excepto si es el mismo)
        if (!linea.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                lineaProductoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe una línea de producto con el nombre: " + dto.getNombre());
        }

        // Actualizar
        linea.setNombre(dto.getNombre());
        linea.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            linea.setActivo(dto.getActivo());
        }

        LineaProductoModel updated = lineaProductoRepository.save(linea);
        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public LineaProductoResponseDTO obtenerPorId(Long id) {
        LineaProductoModel linea = lineaProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Línea de producto no encontrada con id: " + id));
        return mapToResponseDTO(linea);
    }

    @Transactional(readOnly = true)
    public List<LineaProductoResponseDTO> listar() {
        return lineaProductoRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LineaProductoResponseDTO> listarActivos() {
        return lineaProductoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LineaProductoResponseDTO> buscar(String nombre) {
        return lineaProductoRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id) {
        LineaProductoModel linea = lineaProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Línea de producto no encontrada con id: " + id));
        
        // Soft delete
        linea.setActivo(false);
        lineaProductoRepository.save(linea);
    }

    private LineaProductoResponseDTO mapToResponseDTO(LineaProductoModel linea) {
        return LineaProductoResponseDTO.builder()
                .id(linea.getId())
                .nombre(linea.getNombre())
                .descripcion(linea.getDescripcion())
                .activo(linea.getActivo())
                .fechaRegistro(linea.getFechaRegistro())
                .fechaActualizacion(linea.getFechaActualizacion())
                .build();
    }
}