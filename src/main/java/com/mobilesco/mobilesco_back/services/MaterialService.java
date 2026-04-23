// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/MaterialService.java
// ============================================
package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.material.MaterialCreateDTO;
import com.mobilesco.mobilesco_back.dto.material.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.dto.material.MaterialUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.MaterialModel;
import com.mobilesco.mobilesco_back.repositories.MaterialRepository;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    // ========== MAPPER ==========
    
    private MaterialResponseDTO mapToResponseDTO(MaterialModel material) {
        MaterialResponseDTO dto = new MaterialResponseDTO();
        dto.setId(material.getId());
        dto.setNombre(material.getNombre());
        dto.setDescripcion(material.getDescripcion());
        dto.setActivo(material.getActivo());
        dto.setCreatedAt(material.getCreatedAt());
        return dto;
    }
    
    private List<MaterialResponseDTO> mapToResponseDTOList(List<MaterialModel> materiales) {
        return materiales.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // ========== CREATE ==========
    
    public MaterialResponseDTO crear(MaterialCreateDTO dto) {
        
        if (materialRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe un material con el nombre: " + dto.getNombre());
        }
        
        MaterialModel material = new MaterialModel();
        material.setNombre(dto.getNombre());
        material.setDescripcion(dto.getDescripcion());
        material.setActivo(true);
        
        MaterialModel guardado = materialRepository.save(material);
        return mapToResponseDTO(guardado);
    }
    
    // ========== READ ==========
    
    public List<MaterialResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(materialRepository.findAll());
    }
    
    public List<MaterialResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(materialRepository.findByActivo(true));
    }
    
    public MaterialResponseDTO obtenerPorId(Long id) {
        MaterialModel material = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Material no encontrado con ID: " + id));
        return mapToResponseDTO(material);
    }
    
    public MaterialResponseDTO obtenerPorNombre(String nombre) {
        MaterialModel material = materialRepository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Material no encontrado con nombre: " + nombre));
        return mapToResponseDTO(material);
    }
    
    // ========== UPDATE ==========
    
    public MaterialResponseDTO actualizar(Long id, MaterialUpdateDTO dto) {
        
        MaterialModel existente = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Material no encontrado con ID: " + id));
        
        // Validar nombre único
        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (materialRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe un material con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre());
        }
        
        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }
        
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }
        
        MaterialModel actualizado = materialRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }
    
    // ========== DELETE ==========
    
    public void eliminar(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new NotFoundException("Material no encontrado con ID: " + id);
        }
        materialRepository.deleteById(id);
    }
}