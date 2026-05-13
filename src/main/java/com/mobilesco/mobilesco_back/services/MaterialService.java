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
import com.mobilesco.mobilesco_back.repositories.ProductoRepository;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final ProductoRepository productoRepository;

    public MaterialService(MaterialRepository materialRepository, ProductoRepository productoRepository) {
        this.materialRepository = materialRepository;
        this.productoRepository = productoRepository;
    }

    public MaterialResponseDTO crear(MaterialCreateDTO dto) {
        validarDuplicados(dto.getCodigo(), dto.getNombre(), null);

        MaterialModel material = MaterialModel.builder()
                .codigo(dto.getCodigo().trim().toUpperCase())
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();

        return mapToResponseDTO(materialRepository.save(material));
    }

    public MaterialResponseDTO actualizar(Long id, MaterialUpdateDTO dto) {
        MaterialModel material = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Material no encontrado con ID: " + id));

        validarDuplicados(dto.getCodigo(), dto.getNombre(), id);

        material.setCodigo(dto.getCodigo().trim().toUpperCase());
        material.setNombre(dto.getNombre().trim());
        material.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            material.setActivo(dto.getActivo());
        }

        return mapToResponseDTO(materialRepository.save(material));
    }

    public List<MaterialResponseDTO> obtenerTodos() {
        return materialRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<MaterialResponseDTO> obtenerActivos() {
        return materialRepository.findByActivo(true)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public MaterialResponseDTO obtenerPorId(Long id) {
        return materialRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new NotFoundException("Material no encontrado con ID: " + id));
    }

    public MaterialResponseDTO activar(Long id) {
        MaterialModel material = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Material no encontrado con ID: " + id));
        material.setActivo(true);
        return mapToResponseDTO(materialRepository.save(material));
    }

    public MaterialResponseDTO desactivar(Long id) {
        MaterialModel material = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Material no encontrado con ID: " + id));
        material.setActivo(false);
        return mapToResponseDTO(materialRepository.save(material));
    }

    public void eliminar(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new NotFoundException("Material no encontrado con ID: " + id);
        }

        if (productoRepository.existsByMaterialId(id)) {
            throw new BadRequestException("No se puede eliminar el material porque tiene productos asociados");
        }

        materialRepository.deleteById(id);
    }

    private void validarDuplicados(String codigo, String nombre, Long idActual) {
        materialRepository.findByCodigo(codigo.trim().toUpperCase())
                .filter(material -> !material.getId().equals(idActual))
                .ifPresent(material -> {
                    throw new BadRequestException("Ya existe un material con el codigo: " + codigo);
                });

        materialRepository.findByNombre(nombre.trim())
                .filter(material -> !material.getId().equals(idActual))
                .ifPresent(material -> {
                    throw new BadRequestException("Ya existe un material con el nombre: " + nombre);
                });
    }

    private MaterialResponseDTO mapToResponseDTO(MaterialModel material) {
        MaterialResponseDTO dto = new MaterialResponseDTO();
        dto.setId(material.getId());
        dto.setCodigo(material.getCodigo());
        dto.setNombre(material.getNombre());
        dto.setDescripcion(material.getDescripcion());
        dto.setActivo(material.getActivo());
        dto.setFechaRegistro(material.getFechaRegistro());
        dto.setFechaActualizacion(material.getFechaActualizacion());
        return dto;
    }
}
