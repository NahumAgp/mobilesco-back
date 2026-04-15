package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.Material.MaterialCreateDTO;
import com.mobilesco.mobilesco_back.dto.Material.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.dto.Material.MaterialUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.MaterialModel;
import com.mobilesco.mobilesco_back.repositories.MaterialRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    @Transactional
    public MaterialResponseDTO crear(MaterialCreateDTO dto) {
        log.info("Creando nuevo material: {}", dto.getNombre());
        
        // Validar nombre único
        if (materialRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe un material con el nombre: " + dto.getNombre());
        }

        // Crear entidad
        MaterialModel material = MaterialModel.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();

        MaterialModel saved = materialRepository.save(material);
        log.info("Material creado con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }

    @Transactional
    public MaterialResponseDTO actualizar(Long id, MaterialUpdateDTO dto) {
        log.info("Actualizando material ID: {}", id);
        
        MaterialModel material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con id: " + id));

        // Validar nombre único (excepto si es el mismo)
        if (!material.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                materialRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe un material con el nombre: " + dto.getNombre());
        }

        // Actualizar
        material.setNombre(dto.getNombre());
        material.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            material.setActivo(dto.getActivo());
        }

        MaterialModel updated = materialRepository.save(material);
        log.info("Material actualizado: {}", updated.getNombre());
        
        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public MaterialResponseDTO obtenerPorId(Long id) {
        MaterialModel material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con id: " + id));
        return mapToResponseDTO(material);
    }

    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> listar() {
        return materialRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> listarActivos() {
        return materialRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> buscar(String nombre) {
        return materialRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando (desactivando) material ID: {}", id);
        
        MaterialModel material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con id: " + id));
        
        material.setActivo(false);
        materialRepository.save(material);
        
        log.info("Material desactivado correctamente");
    }

    private MaterialResponseDTO mapToResponseDTO(MaterialModel material) {
        return MaterialResponseDTO.builder()
                .id(material.getId())
                .nombre(material.getNombre())
                .descripcion(material.getDescripcion())
                .activo(material.getActivo())
                .fechaRegistro(material.getFechaRegistro())
                .fechaActualizacion(material.getFechaActualizacion())
                .build();
    }
}