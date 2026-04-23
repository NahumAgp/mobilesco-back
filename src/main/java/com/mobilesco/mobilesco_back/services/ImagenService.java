// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/ImagenService.java
// ============================================
package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.imagen.ImagenCreateDTO;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenResponseDTO;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.ImagenModel;
import com.mobilesco.mobilesco_back.models.VarianteModel;
import com.mobilesco.mobilesco_back.repositories.ImagenRepository;
import com.mobilesco.mobilesco_back.repositories.VarianteRepository;

@Service
public class ImagenService {

    private final ImagenRepository imagenRepository;
    private final VarianteRepository varianteRepository;

    public ImagenService(ImagenRepository imagenRepository, VarianteRepository varianteRepository) {
        this.imagenRepository = imagenRepository;
        this.varianteRepository = varianteRepository;
    }

    // ========== MAPPER ==========
    
    private ImagenResponseDTO mapToResponseDTO(ImagenModel imagen) {
        ImagenResponseDTO dto = new ImagenResponseDTO();
        dto.setId(imagen.getId());
        dto.setUrl(imagen.getUrl());
        dto.setEsPrincipal(imagen.getEsPrincipal());
        dto.setOrden(imagen.getOrden());
        dto.setAltTexto(imagen.getAltTexto());
        dto.setCreatedAt(imagen.getCreatedAt());
        if (imagen.getVariante() != null) {
            dto.setVarianteId(imagen.getVariante().getId());
        }
        return dto;
    }
    
    private List<ImagenResponseDTO> mapToResponseDTOList(List<ImagenModel> imagenes) {
        return imagenes.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // ========== CREATE ==========
    
    @Transactional
    public ImagenResponseDTO crear(ImagenCreateDTO dto) {
        
        VarianteModel variante = varianteRepository.findById(dto.getVarianteId())
                .orElseThrow(() -> new NotFoundException("Variante no encontrada con ID: " + dto.getVarianteId()));
        
        // Si es la primera imagen de la variante, forzar como principal
        boolean esPrimeraImagen = imagenRepository.countByVarianteId(variante.getId()) == 0;
        
        ImagenModel imagen = new ImagenModel();
        imagen.setUrl(dto.getUrl());
        imagen.setAltTexto(dto.getAltTexto());
        imagen.setVariante(variante);
        
        // Manejar imagen principal
        if (esPrimeraImagen || (dto.getEsPrincipal() != null && dto.getEsPrincipal())) {
            // Resetear flag principal en otras imágenes de esta variante
            imagenRepository.resetPrincipalFlag(variante.getId());
            imagen.setEsPrincipal(true);
        } else {
            imagen.setEsPrincipal(false);
        }
        
        if (dto.getOrden() != null) {
            imagen.setOrden(dto.getOrden());
        }
        
        ImagenModel guardado = imagenRepository.save(imagen);
        return mapToResponseDTO(guardado);
    }
    
    // ========== READ ==========
    
    public List<ImagenResponseDTO> obtenerPorVariante(Long varianteId) {
        if (!varianteRepository.existsById(varianteId)) {
            throw new NotFoundException("Variante no encontrada con ID: " + varianteId);
        }
        return mapToResponseDTOList(imagenRepository.findByVarianteIdOrderByOrdenAsc(varianteId));
    }
    
    public ImagenResponseDTO obtenerPrincipalPorVariante(Long varianteId) {
        ImagenModel imagen = imagenRepository.findByVarianteIdAndEsPrincipalTrue(varianteId)
                .orElse(null);
        return imagen != null ? mapToResponseDTO(imagen) : null;
    }
    
    public ImagenResponseDTO obtenerPorId(Long id) {
        ImagenModel imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagen no encontrada con ID: " + id));
        return mapToResponseDTO(imagen);
    }
    
    // ========== UPDATE ==========
    
    @Transactional
    public ImagenResponseDTO actualizar(Long id, ImagenUpdateDTO dto) {
        
        ImagenModel existente = imagenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagen no encontrada con ID: " + id));
        
        if (dto.getUrl() != null) {
            existente.setUrl(dto.getUrl());
        }
        
        if (dto.getAltTexto() != null) {
            existente.setAltTexto(dto.getAltTexto());
        }
        
        if (dto.getOrden() != null) {
            existente.setOrden(dto.getOrden());
        }
        
        // Manejar cambio de imagen principal
        if (dto.getEsPrincipal() != null && dto.getEsPrincipal() && !existente.getEsPrincipal()) {
            // Resetear flag principal en otras imágenes de esta variante
            imagenRepository.resetPrincipalFlag(existente.getVariante().getId());
            existente.setEsPrincipal(true);
        } else if (dto.getEsPrincipal() != null && !dto.getEsPrincipal() && existente.getEsPrincipal()) {
            // No permitir desmarcar la única imagen principal
            long totalPrincipales = imagenRepository.findByVarianteIdAndEsPrincipalTrue(existente.getVariante().getId()).stream().count();
            if (totalPrincipales <= 1) {
                throw new BadRequestException("No se puede desmarcar la única imagen principal. Debe marcar otra como principal primero.");
            }
            existente.setEsPrincipal(false);
        }
        
        ImagenModel actualizado = imagenRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }
    
    // ========== DELETE ==========
    
    @Transactional
    public void eliminar(Long id) {
        
        ImagenModel imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagen no encontrada con ID: " + id));
        
        Long varianteId = imagen.getVariante().getId();
        boolean eraPrincipal = imagen.getEsPrincipal();
        
        imagenRepository.deleteById(id);
        
        // Si se eliminó la imagen principal y quedan otras imágenes, promover la primera como principal
        if (eraPrincipal) {
            List<ImagenModel> restantes = imagenRepository.findByVarianteIdOrderByOrdenAsc(varianteId);
            if (!restantes.isEmpty()) {
                ImagenModel nuevaPrincipal = restantes.get(0);
                nuevaPrincipal.setEsPrincipal(true);
                imagenRepository.save(nuevaPrincipal);
            }
        }
    }
    
    // ========== BULK DELETE ==========
    
    @Transactional
    public void eliminarTodasPorVariante(Long varianteId) {
        List<ImagenModel> imagenes = imagenRepository.findByVarianteId(varianteId);
        imagenRepository.deleteAll(imagenes);
    }
}
