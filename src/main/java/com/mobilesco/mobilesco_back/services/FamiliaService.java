// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/FamiliaService.java
// ============================================
package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.dto.familia.FamiliaCreateDTO;
import com.mobilesco.mobilesco_back.dto.familia.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.dto.familia.FamiliaUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.FamiliaModel;
import com.mobilesco.mobilesco_back.models.LineaModel;
import com.mobilesco.mobilesco_back.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.repositories.LineaRepository;

@Service
public class FamiliaService {

    private static final int PAGE_SIZE = 10;

    private final FamiliaRepository familiaRepository;
    private final LineaRepository lineaRepository;

    public FamiliaService(FamiliaRepository familiaRepository, LineaRepository lineaRepository) {
        this.familiaRepository = familiaRepository;
        this.lineaRepository = lineaRepository;
    }

    // ========== MAPPER ==========

    private FamiliaResponseDTO mapToResponseDTO(FamiliaModel familia) {
        FamiliaResponseDTO dto = new FamiliaResponseDTO();
        dto.setId(familia.getId());
        dto.setCodigo(familia.getCodigo());
        dto.setNombre(familia.getNombre());
        dto.setDescripcion(familia.getDescripcion());
        dto.setActivo(familia.getActivo());
        dto.setCreatedAt(familia.getCreatedAt());

        if (familia.getLinea() != null) {
            dto.setLineaId(familia.getLinea().getId());
            dto.setLineaNombre(familia.getLinea().getNombre());
        }

        return dto;
    }

    private List<FamiliaResponseDTO> mapToResponseDTOList(List<FamiliaModel> familias) {
        return familias.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== CREATE ==========

    public FamiliaResponseDTO crear(FamiliaCreateDTO dto) {

        if (familiaRepository.existsByCodigo(dto.getCodigo())) {
            throw new BadRequestException("Ya existe una familia con el codigo: " + dto.getCodigo());
        }

        if (familiaRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe una familia con el nombre: " + dto.getNombre());
        }

        LineaModel linea = lineaRepository.findById(dto.getLineaId())
                .orElseThrow(() -> new NotFoundException("Linea no encontrada con ID: " + dto.getLineaId()));

        FamiliaModel familia = new FamiliaModel();
        familia.setCodigo(dto.getCodigo());
        familia.setNombre(dto.getNombre());
        familia.setDescripcion(dto.getDescripcion());
        familia.setLinea(linea);
        familia.setActivo(true);

        FamiliaModel guardado = familiaRepository.save(familia);
        return mapToResponseDTO(guardado);
    }

    // ========== READ ==========

    private Sort construirSortFamilias(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
        }

        String campo = sortBy.trim();
        String campoNormalizado = campo.toLowerCase(Locale.ROOT);

        switch (campoNormalizado) {
            case "id":
                return Sort.by(sortDirection, "id");
            case "codigo":
                return Sort.by(sortDirection, "codigo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "nombre":
                return Sort.by(sortDirection, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
            case "descripcion":
                return Sort.by(sortDirection, "descripcion").and(Sort.by(Sort.Direction.ASC, "id"));
            case "activo":
                return Sort.by(sortDirection, "activo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "createdat":
            case "created_at":
                return Sort.by(sortDirection, "createdAt").and(Sort.by(Sort.Direction.ASC, "id"));
            default:
                return Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
        }
    }

    public List<FamiliaResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(familiaRepository.findAll());
    }

    public PageResponseDTO<FamiliaResponseDTO> obtenerPaginado(int page, String sortBy, String direction) {
        int pageNumber = Math.max(page, 0);
        PageRequest pageable = PageRequest.of(pageNumber, PAGE_SIZE, construirSortFamilias(sortBy, direction));

        Page<FamiliaResponseDTO> result = familiaRepository.findAll(pageable).map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public List<FamiliaResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(familiaRepository.findByActivo(true));
    }

    public FamiliaResponseDTO obtenerPorId(Long id) {
        FamiliaModel familia = familiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + id));
        return mapToResponseDTO(familia);
    }

    public List<FamiliaResponseDTO> obtenerPorLinea(Long lineaId) {
        if (!lineaRepository.existsById(lineaId)) {
            throw new NotFoundException("Linea no encontrada con ID: " + lineaId);
        }
        return mapToResponseDTOList(familiaRepository.findByLineaId(lineaId));
    }

    public List<FamiliaResponseDTO> obtenerPorLineaYActivo(Long lineaId, Boolean activo) {
        return mapToResponseDTOList(familiaRepository.findByLineaIdAndActivo(lineaId, activo));
    }

    // ========== UPDATE ==========

    public FamiliaResponseDTO actualizar(Long id, FamiliaUpdateDTO dto) {

        FamiliaModel existente = familiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + id));

        if (dto.getCodigo() != null && !dto.getCodigo().equals(existente.getCodigo())) {
            if (familiaRepository.existsByCodigo(dto.getCodigo())) {
                throw new BadRequestException("Ya existe una familia con el codigo: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo());
        }

        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (familiaRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe una familia con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre());
        }

        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }

        if (dto.getLineaId() != null) {
            LineaModel linea = lineaRepository.findById(dto.getLineaId())
                    .orElseThrow(() -> new NotFoundException("Linea no encontrada con ID: " + dto.getLineaId()));
            existente.setLinea(linea);
        }

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        FamiliaModel actualizado = familiaRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }

    // ========== DELETE ==========

    public void eliminar(Long id) {
        if (!familiaRepository.existsById(id)) {
            throw new NotFoundException("Familia no encontrada con ID: " + id);
        }
        familiaRepository.deleteById(id);
    }
}
