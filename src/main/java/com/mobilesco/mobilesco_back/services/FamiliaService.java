package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.Familia.FamiliaCreateDTO;
import com.mobilesco.mobilesco_back.dto.Familia.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.dto.Familia.FamiliaUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.FamiliaModel;
import com.mobilesco.mobilesco_back.repositories.FamiliaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamiliaService {

    private final FamiliaRepository familiaRepository;

    @Transactional
    public FamiliaResponseDTO crear(FamiliaCreateDTO dto) {
        log.info("Creando nueva familia: {}", dto.getNombre());

        // Validar nombre único según padre
        if (dto.getPadreId() == null) {
            if (familiaRepository.existsByNombreAndPadreIsNull(dto.getNombre())) {
                throw new ValidationException("Ya existe una familia con ese nombre en el nivel raíz");
            }
        } else {
            if (familiaRepository.existsByNombreAndPadreId(dto.getNombre(), dto.getPadreId())) {
                throw new ValidationException("Ya existe una familia con ese nombre bajo el mismo padre");
            }
        }

        // Buscar padre si existe
        FamiliaModel padre = null;
        if (dto.getPadreId() != null) {
            padre = familiaRepository.findById(dto.getPadreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Padre no encontrado con id: " + dto.getPadreId()));
        }

        // Crear entidad
        FamiliaModel familia = FamiliaModel.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .padre(padre)
                .activo(true)
                .build();

        FamiliaModel saved = familiaRepository.save(familia);
        log.info("Familia creada con ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    @Transactional
    public FamiliaResponseDTO actualizar(Long id, FamiliaUpdateDTO dto) {
        log.info("Actualizando familia ID: {}", id);

        FamiliaModel familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + id));

        // 🔴 VALIDACIÓN CORREGIDA - Permite el mismo nombre en la misma familia
        validarNombreUnicoEnActualizacion(id, dto.getNombre(), dto.getPadreId());

        // Actualizar padre si cambió
        if (dto.getPadreId() != null) {
            if (familia.getPadre() == null || !familia.getPadre().getId().equals(dto.getPadreId())) {
                // Validar que no se asigne a sí mismo como padre
                if (dto.getPadreId().equals(id)) {
                    throw new ValidationException("Una familia no puede ser padre de sí misma");
                }
                
                FamiliaModel nuevoPadre = familiaRepository.findById(dto.getPadreId())
                        .orElseThrow(() -> new ResourceNotFoundException("Padre no encontrado con id: " + dto.getPadreId()));
                familia.setPadre(nuevoPadre);
            }
        } else {
            familia.setPadre(null);
        }

        // Actualizar campos
        familia.setNombre(dto.getNombre());
        familia.setDescripcion(dto.getDescripcion());
        
        if (dto.getActivo() != null) {
            familia.setActivo(dto.getActivo());
        }

        FamiliaModel updated = familiaRepository.save(familia);
        log.info("Familia actualizada correctamente");

        return mapToResponseDTO(updated);
    }

    /**
     * Valida que no exista otra familia con el mismo nombre y padre (excluyendo la actual)
     */
    private void validarNombreUnicoEnActualizacion(Long id, String nombre, Long padreId) {
        boolean existeOtraFamilia;
        
        if (padreId == null) {
            // Buscar otra familia raíz con el mismo nombre (excluyendo esta)
            existeOtraFamilia = familiaRepository.existsByNombreAndPadreIsNullAndIdNot(nombre, id);
        } else {
            // Buscar otra familia bajo el mismo padre con el mismo nombre (excluyendo esta)
            existeOtraFamilia = familiaRepository.existsByNombreAndPadreIdAndIdNot(nombre, padreId, id);
        }
        
        if (existeOtraFamilia) {
            throw new ValidationException("Ya existe una familia con ese nombre en el mismo nivel");
        }
    }

    @Transactional(readOnly = true)
    public FamiliaResponseDTO obtenerPorId(Long id) {
        FamiliaModel familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + id));
        return mapToResponseDTO(familia);
    }

    @Transactional(readOnly = true)
    public List<FamiliaResponseDTO> listar() {
        return familiaRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FamiliaResponseDTO> listarActivas() {
        return familiaRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
   public void eliminar(Long id) {
    log.info("Eliminando (desactivando) familia ID: {}", id);

    FamiliaModel familia = familiaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada"));

    // Verificar si tiene hijos activos
    List<FamiliaModel> hijos = familiaRepository.findByPadreIdAndActivoTrue(id);
    if (!hijos.isEmpty()) {
        throw new ValidationException("No se puede eliminar una familia que tiene hijos activos");
    }

    familia.setActivo(false);
    familiaRepository.save(familia);
    log.info("Familia desactivada correctamente");
}

private FamiliaResponseDTO mapToResponseDTO(FamiliaModel familia) {
    return FamiliaResponseDTO.builder()
            .id(familia.getId())
            .nombre(familia.getNombre())
            .descripcion(familia.getDescripcion())
            .padreId(familia.getPadre() != null ? familia.getPadre().getId() : null)
            .padreNombre(familia.getPadre() != null ? familia.getPadre().getNombre() : null)
            .activo(familia.getActivo())  // 🔴 CORREGIDO
            .fechaRegistro(familia.getFechaRegistro())
            .fechaActualizacion(familia.getFechaActualizacion())
            .build();
}
}