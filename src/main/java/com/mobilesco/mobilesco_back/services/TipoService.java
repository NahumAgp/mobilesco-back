// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/TipoService.java
// ============================================
package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.tipo.TipoCreateDTO;
import com.mobilesco.mobilesco_back.dto.tipo.TipoResponseDTO;
import com.mobilesco.mobilesco_back.dto.tipo.TipoUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.TipoModel;
import com.mobilesco.mobilesco_back.repositories.TipoRepository;

@Service
public class TipoService {

    private final TipoRepository tipoRepository;

    public TipoService(TipoRepository tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    // ========== MAPPER ==========
    
    private TipoResponseDTO mapToResponseDTO(TipoModel tipo) {
        TipoResponseDTO dto = new TipoResponseDTO();
        dto.setId(tipo.getId());
        dto.setNombre(tipo.getNombre());
        dto.setDescripcion(tipo.getDescripcion());
        dto.setActivo(tipo.getActivo());
        dto.setCreatedAt(tipo.getCreatedAt());
        return dto;
    }
    
    private List<TipoResponseDTO> mapToResponseDTOList(List<TipoModel> tipos) {
        return tipos.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // ========== CREATE ==========
    
    public TipoResponseDTO crear(TipoCreateDTO dto) {
        
        if (tipoRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe un tipo con el nombre: " + dto.getNombre());
        }
        
        TipoModel tipo = new TipoModel();
        tipo.setNombre(dto.getNombre());
        tipo.setDescripcion(dto.getDescripcion());
        tipo.setActivo(true);
        
        TipoModel guardado = tipoRepository.save(tipo);
        return mapToResponseDTO(guardado);
    }
    
    // ========== READ ==========
    
    public List<TipoResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(tipoRepository.findAll());
    }
    
    public List<TipoResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(tipoRepository.findByActivo(true));
    }
    
    public TipoResponseDTO obtenerPorId(Long id) {
        TipoModel tipo = tipoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo no encontrado con ID: " + id));
        return mapToResponseDTO(tipo);
    }
    
    public TipoResponseDTO obtenerPorNombre(String nombre) {
        TipoModel tipo = tipoRepository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Tipo no encontrado con nombre: " + nombre));
        return mapToResponseDTO(tipo);
    }
    
    // ========== UPDATE ==========
    
    public TipoResponseDTO actualizar(Long id, TipoUpdateDTO dto) {
        
        TipoModel existente = tipoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo no encontrado con ID: " + id));
        
        // Validar nombre único
        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (tipoRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe un tipo con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre());
        }
        
        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }
        
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }
        
        TipoModel actualizado = tipoRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }
    
    // ========== DELETE ==========
    
    public void eliminar(Long id) {
        if (!tipoRepository.existsById(id)) {
            throw new NotFoundException("Tipo no encontrado con ID: " + id);
        }
        tipoRepository.deleteById(id);
    }
}