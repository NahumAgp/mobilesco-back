// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/EstiloService.java
// ============================================
package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.estilo.EstiloCreateDTO;
import com.mobilesco.mobilesco_back.dto.estilo.EstiloResponseDTO;
import com.mobilesco.mobilesco_back.dto.estilo.EstiloUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.EstiloModel;
import com.mobilesco.mobilesco_back.models.FamiliaModel;
import com.mobilesco.mobilesco_back.repositories.EstiloRepository;
import com.mobilesco.mobilesco_back.repositories.FamiliaRepository;

@Service
public class EstiloService {

    private final EstiloRepository estiloRepository;
    private final FamiliaRepository familiaRepository;

    public EstiloService(EstiloRepository estiloRepository, FamiliaRepository familiaRepository) {
        this.estiloRepository = estiloRepository;
        this.familiaRepository = familiaRepository;
    }

    // ========== MAPPER ==========
    
    private EstiloResponseDTO mapToResponseDTO(EstiloModel estilo) {
        EstiloResponseDTO dto = new EstiloResponseDTO();
        dto.setId(estilo.getId());
        dto.setNombre(estilo.getNombre());
        dto.setDescripcion(estilo.getDescripcion());
        dto.setActivo(estilo.getActivo());
        dto.setCreatedAt(estilo.getCreatedAt());
        
        if (estilo.getFamilia() != null) {
            dto.setFamiliaId(estilo.getFamilia().getId());
            dto.setFamiliaNombre(estilo.getFamilia().getNombre());
        }
        
        return dto;
    }
    
    private List<EstiloResponseDTO> mapToResponseDTOList(List<EstiloModel> estilos) {
        return estilos.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // ========== CREATE ==========
    
    public EstiloResponseDTO crear(EstiloCreateDTO dto) {
        
        if (estiloRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe un estilo con el nombre: " + dto.getNombre());
        }
        
        FamiliaModel familia = familiaRepository.findById(dto.getFamiliaId())
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + dto.getFamiliaId()));
        
        EstiloModel estilo = new EstiloModel();
        estilo.setNombre(dto.getNombre());
        estilo.setDescripcion(dto.getDescripcion());
        estilo.setFamilia(familia);
        estilo.setActivo(true);
        
        EstiloModel guardado = estiloRepository.save(estilo);
        return mapToResponseDTO(guardado);
    }
    
    // ========== READ ==========
    
    public List<EstiloResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(estiloRepository.findAll());
    }
    
    public List<EstiloResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(estiloRepository.findByActivo(true));
    }
    
    public EstiloResponseDTO obtenerPorId(Long id) {
        EstiloModel estilo = estiloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estilo no encontrado con ID: " + id));
        return mapToResponseDTO(estilo);
    }
    
    public List<EstiloResponseDTO> obtenerPorFamilia(Long familiaId) {
        if (!familiaRepository.existsById(familiaId)) {
            throw new NotFoundException("Familia no encontrada con ID: " + familiaId);
        }
        return mapToResponseDTOList(estiloRepository.findByFamiliaId(familiaId));
    }
    
    public List<EstiloResponseDTO> obtenerPorFamiliaYActivo(Long familiaId, Boolean activo) {
        return mapToResponseDTOList(estiloRepository.findByFamiliaIdAndActivo(familiaId, activo));
    }
    
    // ========== UPDATE ==========
    
    public EstiloResponseDTO actualizar(Long id, EstiloUpdateDTO dto) {
        
        EstiloModel existente = estiloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estilo no encontrado con ID: " + id));
        
        // Validar nombre único
        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (estiloRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe un estilo con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre());
        }
        
        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }
        
        if (dto.getFamiliaId() != null) {
            FamiliaModel familia = familiaRepository.findById(dto.getFamiliaId())
                    .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + dto.getFamiliaId()));
            existente.setFamilia(familia);
        }
        
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }
        
        EstiloModel actualizado = estiloRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }
    
    // ========== DELETE ==========
    
    public void eliminar(Long id) {
        if (!estiloRepository.existsById(id)) {
            throw new NotFoundException("Estilo no encontrado con ID: " + id);
        }
        estiloRepository.deleteById(id);
    }
}