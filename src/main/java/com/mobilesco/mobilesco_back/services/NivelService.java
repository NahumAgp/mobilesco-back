
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/NivelService.java
package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.nivel.NivelCreateDTO;
import com.mobilesco.mobilesco_back.dto.nivel.NivelResponseDTO;
import com.mobilesco.mobilesco_back.dto.nivel.NivelUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.NivelModel;
import com.mobilesco.mobilesco_back.repositories.NivelRepository;

@Service
public class NivelService {

    private final NivelRepository nivelRepository;

    public NivelService(NivelRepository nivelRepository) {
        this.nivelRepository = nivelRepository;
    }

    // ========== MAPPER ==========
    
    private NivelResponseDTO mapToResponseDTO(NivelModel nivel) {
        NivelResponseDTO dto = new NivelResponseDTO();
        dto.setId(nivel.getId());
        dto.setCodigo(nivel.getCodigo());
        dto.setNombre(nivel.getNombre());
        dto.setDescripcion(nivel.getDescripcion());
        dto.setActivo(nivel.getActivo());
        dto.setCreatedAt(nivel.getCreatedAt());
        return dto;
    }
    
    private List<NivelResponseDTO> mapToResponseDTOList(List<NivelModel> niveles) {
        return niveles.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // ========== CREATE ==========
    
    public NivelResponseDTO crear(NivelCreateDTO dto) {
        
        if (nivelRepository.existsByCodigo(dto.getCodigo())) {
            throw new BadRequestException("Ya existe un nivel con el código: " + dto.getCodigo());
        }
        
        if (nivelRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe un nivel con el nombre: " + dto.getNombre());
        }
        
        NivelModel nivel = new NivelModel();
        nivel.setCodigo(dto.getCodigo());
        nivel.setNombre(dto.getNombre());
        nivel.setDescripcion(dto.getDescripcion());
        nivel.setActivo(true);
        
        NivelModel guardado = nivelRepository.save(nivel);
        return mapToResponseDTO(guardado);
    }
    
    // ========== READ ==========
    
    public List<NivelResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(nivelRepository.findAll());
    }
    
    public List<NivelResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(nivelRepository.findByActivo(true));
    }
    
    public NivelResponseDTO obtenerPorId(Long id) {
        NivelModel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con ID: " + id));
        return mapToResponseDTO(nivel);
    }
    
    public NivelResponseDTO obtenerPorCodigo(String codigo) {
        NivelModel nivel = nivelRepository.findByCodigo(codigo)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con código: " + codigo));
        return mapToResponseDTO(nivel);
    }
    
    public NivelResponseDTO obtenerPorNombre(String nombre) {
        NivelModel nivel = nivelRepository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con nombre: " + nombre));
        return mapToResponseDTO(nivel);
    }
    
    // ========== UPDATE ==========
    
    public NivelResponseDTO actualizar(Long id, NivelUpdateDTO dto) {
        
        NivelModel existente = nivelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con ID: " + id));
        
        // Validar código único
        if (dto.getCodigo() != null && !dto.getCodigo().equals(existente.getCodigo())) {
            if (nivelRepository.existsByCodigo(dto.getCodigo())) {
                throw new BadRequestException("Ya existe un nivel con el código: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo());
        }
        
        // Validar nombre único
        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (nivelRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe un nivel con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre());
        }
        
        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }
        
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }
        
        NivelModel actualizado = nivelRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }
    
    // ========== DELETE ==========
    
    public void eliminar(Long id) {
        if (!nivelRepository.existsById(id)) {
            throw new NotFoundException("Nivel no encontrado con ID: " + id);
        }
        nivelRepository.deleteById(id);
    }
}
