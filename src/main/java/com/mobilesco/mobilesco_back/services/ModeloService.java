package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.modelo.ModeloCreateDTO;
import com.mobilesco.mobilesco_back.dto.modelo.ModeloResponseDTO;
import com.mobilesco.mobilesco_back.dto.modelo.ModeloUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.FamiliaModel;
import com.mobilesco.mobilesco_back.models.ModeloModel;
import com.mobilesco.mobilesco_back.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.repositories.ModeloRepository;

@Service
public class ModeloService {

    private final ModeloRepository modeloRepository;
    private final FamiliaRepository familiaRepository;

    public ModeloService(ModeloRepository modeloRepository,
                               FamiliaRepository familiaRepository) {
        this.modeloRepository = modeloRepository;
        this.familiaRepository = familiaRepository;
    }

    private ModeloResponseDTO mapToResponseDTO(ModeloModel modelo) {
        ModeloResponseDTO dto = new ModeloResponseDTO();
        dto.setId(modelo.getId());
        dto.setCodigo(modelo.getCodigo());
        dto.setNombre(modelo.getNombre());
        dto.setDescripcion(modelo.getDescripcion());
        dto.setUrlImagen(modelo.getUrlImagen());
        dto.setActivo(modelo.getActivo());
        dto.setCreatedAt(modelo.getCreatedAt());
        dto.setUpdatedAt(modelo.getUpdatedAt());

        if (modelo.getFamilia() != null) {
            dto.setFamiliaId(modelo.getFamilia().getId());
            dto.setFamiliaNombre(modelo.getFamilia().getNombre());
        }

        return dto;
    }

    private List<ModeloResponseDTO> mapToResponseDTOList(List<ModeloModel> modelos) {
        return modelos.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ModeloResponseDTO crear(ModeloCreateDTO dto) {

        if (modeloRepository.existsByCodigo(dto.getCodigo())) {
            throw new BadRequestException("Ya existe un modelo con el codigo: " + dto.getCodigo());
        }

        FamiliaModel familia = familiaRepository.findById(dto.getFamiliaId())
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + dto.getFamiliaId()));

        ModeloModel modelo = new ModeloModel();
        modelo.setCodigo(dto.getCodigo());
        modelo.setNombre(dto.getNombre());
        modelo.setDescripcion(dto.getDescripcion());
        modelo.setUrlImagen(dto.getUrlImagen());
        modelo.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        modelo.setFamilia(familia);

        ModeloModel guardado = modeloRepository.save(modelo);
        return mapToResponseDTO(guardado);
    }

    public List<ModeloResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(modeloRepository.findAll());
    }

    public ModeloResponseDTO obtenerPorId(Long id) {
        ModeloModel modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modelo no encontrado con ID: " + id));
        return mapToResponseDTO(modelo);
    }

    public List<ModeloResponseDTO> obtenerPorFamilia(Long familiaId) {
        if (!familiaRepository.existsById(familiaId)) {
            throw new NotFoundException("Familia no encontrada con ID: " + familiaId);
        }
        return mapToResponseDTOList(modeloRepository.findByFamiliaId(familiaId));
    }

    public List<ModeloResponseDTO> buscarConFiltros(String codigo, String nombre, Long familiaId) {
        return mapToResponseDTOList(modeloRepository.buscarConFiltros(codigo, nombre, familiaId));
    }

    public ModeloResponseDTO actualizar(Long id, ModeloUpdateDTO dto) {

        ModeloModel existente = modeloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modelo no encontrado con ID: " + id));

        if (dto.getCodigo() != null && !dto.getCodigo().equals(existente.getCodigo())) {
            if (modeloRepository.existsByCodigo(dto.getCodigo())) {
                throw new BadRequestException("Ya existe un modelo con el codigo: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo());
        }

        if (dto.getNombre() != null) {
            existente.setNombre(dto.getNombre());
        }

        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }

        if (dto.getUrlImagen() != null) {
            existente.setUrlImagen(dto.getUrlImagen());
        }

        if (dto.getFamiliaId() != null) {
            FamiliaModel familia = familiaRepository.findById(dto.getFamiliaId())
                    .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + dto.getFamiliaId()));
            existente.setFamilia(familia);
        }

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        ModeloModel actualizado = modeloRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }

    public void eliminar(Long id) {
        if (!modeloRepository.existsById(id)) {
            throw new NotFoundException("Modelo no encontrado con ID: " + id);
        }
        modeloRepository.deleteById(id);
    }
}
