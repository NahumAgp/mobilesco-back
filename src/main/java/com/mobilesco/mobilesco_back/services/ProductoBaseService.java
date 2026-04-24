// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/ProductoBaseService.java
// ============================================
package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.productobase.ProductoBaseCreateDTO;
import com.mobilesco.mobilesco_back.dto.productobase.ProductoBaseResponseDTO;
import com.mobilesco.mobilesco_back.dto.productobase.ProductoBaseUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.FamiliaModel;
import com.mobilesco.mobilesco_back.models.ProductoBaseModel;
import com.mobilesco.mobilesco_back.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoBaseRepository;

@Service
public class ProductoBaseService {

    private final ProductoBaseRepository productoBaseRepository;
    private final FamiliaRepository familiaRepository;

    public ProductoBaseService(ProductoBaseRepository productoBaseRepository,
                               FamiliaRepository familiaRepository) {
        this.productoBaseRepository = productoBaseRepository;
        this.familiaRepository = familiaRepository;
    }

    private ProductoBaseResponseDTO mapToResponseDTO(ProductoBaseModel productoBase) {
        ProductoBaseResponseDTO dto = new ProductoBaseResponseDTO();
        dto.setId(productoBase.getId());
        dto.setCodigo(productoBase.getCodigo());
        dto.setNombre(productoBase.getNombre());
        dto.setDescripcion(productoBase.getDescripcion());
        dto.setActivo(productoBase.getActivo());
        dto.setCreatedAt(productoBase.getCreatedAt());
        dto.setUpdatedAt(productoBase.getUpdatedAt());

        if (productoBase.getFamilia() != null) {
            dto.setFamiliaId(productoBase.getFamilia().getId());
            dto.setFamiliaNombre(productoBase.getFamilia().getNombre());
        }

        return dto;
    }

    private List<ProductoBaseResponseDTO> mapToResponseDTOList(List<ProductoBaseModel> productosBase) {
        return productosBase.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductoBaseResponseDTO crear(ProductoBaseCreateDTO dto) {

        if (productoBaseRepository.existsByCodigo(dto.getCodigo())) {
            throw new BadRequestException("Ya existe un producto base con el codigo: " + dto.getCodigo());
        }

        FamiliaModel familia = familiaRepository.findById(dto.getFamiliaId())
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + dto.getFamiliaId()));

        ProductoBaseModel productoBase = new ProductoBaseModel();
        productoBase.setCodigo(dto.getCodigo());
        productoBase.setNombre(dto.getNombre());
        productoBase.setDescripcion(dto.getDescripcion());
        productoBase.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        productoBase.setFamilia(familia);

        ProductoBaseModel guardado = productoBaseRepository.save(productoBase);
        return mapToResponseDTO(guardado);
    }

    public List<ProductoBaseResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(productoBaseRepository.findAll());
    }

    public ProductoBaseResponseDTO obtenerPorId(Long id) {
        ProductoBaseModel productoBase = productoBaseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto base no encontrado con ID: " + id));
        return mapToResponseDTO(productoBase);
    }

    public List<ProductoBaseResponseDTO> obtenerPorFamilia(Long familiaId) {
        if (!familiaRepository.existsById(familiaId)) {
            throw new NotFoundException("Familia no encontrada con ID: " + familiaId);
        }
        return mapToResponseDTOList(productoBaseRepository.findByFamiliaId(familiaId));
    }

    public List<ProductoBaseResponseDTO> buscarConFiltros(String codigo, String nombre, Long familiaId) {
        return mapToResponseDTOList(productoBaseRepository.buscarConFiltros(codigo, nombre, familiaId));
    }

    public ProductoBaseResponseDTO actualizar(Long id, ProductoBaseUpdateDTO dto) {

        ProductoBaseModel existente = productoBaseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto base no encontrado con ID: " + id));

        if (dto.getCodigo() != null && !dto.getCodigo().equals(existente.getCodigo())) {
            if (productoBaseRepository.existsByCodigo(dto.getCodigo())) {
                throw new BadRequestException("Ya existe un producto base con el codigo: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo());
        }

        if (dto.getNombre() != null) {
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

        ProductoBaseModel actualizado = productoBaseRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }

    public void eliminar(Long id) {
        if (!productoBaseRepository.existsById(id)) {
            throw new NotFoundException("Producto base no encontrado con ID: " + id);
        }
        productoBaseRepository.deleteById(id);
    }
}
