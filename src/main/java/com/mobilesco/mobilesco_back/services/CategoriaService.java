package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.Categoria.CategoriaCreateDTO;
import com.mobilesco.mobilesco_back.dto.Categoria.CategoriaResponseDTO;
import com.mobilesco.mobilesco_back.dto.Categoria.CategoriaUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.CategoriaModel;
import com.mobilesco.mobilesco_back.repositories.CategoriaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaResponseDTO crear(CategoriaCreateDTO dto) {
        log.info("Creando nueva categoría: {}", dto.getNombre());
        
        // Validar nombre único
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        // Crear entidad
        CategoriaModel categoria = CategoriaModel.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();

        CategoriaModel saved = categoriaRepository.save(categoria);
        log.info("Categoría creada con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }

    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaUpdateDTO dto) {
        log.info("Actualizando categoría ID: {}", id);
        
        CategoriaModel categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        // Validar nombre único (excepto si es el mismo)
        if (!categoria.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        // Actualizar
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            categoria.setActivo(dto.getActivo());
        }

        CategoriaModel updated = categoriaRepository.save(categoria);
        log.info("Categoría actualizada: {}", updated.getNombre());
        
        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Long id) {
        CategoriaModel categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        return mapToResponseDTO(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarActivos() {
        return categoriaRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> buscar(String nombre) {
        return categoriaRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando (desactivando) categoría ID: {}", id);
        
        CategoriaModel categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
        
        log.info("Categoría desactivada correctamente");
    }

    private CategoriaResponseDTO mapToResponseDTO(CategoriaModel categoria) {
        return CategoriaResponseDTO.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .activo(categoria.getActivo())
                .fechaRegistro(categoria.getFechaRegistro())
                .fechaActualizacion(categoria.getFechaActualizacion())
                .build();
    }
}