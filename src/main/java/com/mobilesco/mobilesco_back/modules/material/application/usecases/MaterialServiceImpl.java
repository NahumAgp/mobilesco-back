/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/material/application/usecases/MaterialServiceImpl.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: MaterialServiceImpl
 * CONTEXTO: Implementacion de casos de uso del modulo; orquesta reglas de negocio y puertos de salida.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.material.application.usecases;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.material.application.ports.MaterialPersistencePort;
import com.mobilesco.mobilesco_back.modules.material.application.ports.ProductoValidationPort;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialCreateDTO;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialUpdateDTO;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;

@Service
public class MaterialServiceImpl implements MaterialUseCase {

    private static final int PAGE_SIZE = 10;

    private final MaterialPersistencePort materialRepository;
    private final ProductoValidationPort productoRepository;

    public MaterialServiceImpl(MaterialPersistencePort materialRepository, ProductoValidationPort productoRepository) {
        this.materialRepository = materialRepository;
        this.productoRepository = productoRepository;
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

    private List<MaterialResponseDTO> mapToResponseDTOList(List<MaterialModel> materiales) {
        return materiales.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private Sort construirSortMateriales(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
        }

        String campo = sortBy.trim().toLowerCase(Locale.ROOT);
        return switch (campo) {
            case "id" -> Sort.by(sortDirection, "id");
            case "codigo" -> Sort.by(sortDirection, "codigo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "nombre" -> Sort.by(sortDirection, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
            case "descripcion" -> Sort.by(sortDirection, "descripcion").and(Sort.by(Sort.Direction.ASC, "id"));
            case "activo" -> Sort.by(sortDirection, "activo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "fecharegistro", "fecha_registro" ->
                    Sort.by(sortDirection, "fechaRegistro").and(Sort.by(Sort.Direction.ASC, "id"));
            case "fechaactualizacion", "fecha_actualizacion" ->
                    Sort.by(sortDirection, "fechaActualizacion").and(Sort.by(Sort.Direction.ASC, "id"));
            default -> Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
        };
    }

    private boolean coincideBusqueda(MaterialResponseDTO material, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        material.getId() != null ? String.valueOf(material.getId()) : null,
                        material.getCodigo(),
                        material.getNombre(),
                        material.getDescripcion(),
                        material.getActivo() != null ? (material.getActivo() ? "activo" : "inactivo") : null,
                        material.getFechaRegistro() != null ? material.getFechaRegistro().toString() : null,
                        material.getFechaActualizacion() != null ? material.getFechaActualizacion().toString() : null)
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(termino));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    public MaterialResponseDTO crear(MaterialCreateDTO dto) {
        validarDuplicados(dto.getCodigo(), dto.getNombre(), null);

        MaterialModel material = MaterialModel.builder()
                .codigo(dto.getCodigo().trim().toUpperCase(Locale.ROOT))
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

        material.setCodigo(dto.getCodigo().trim().toUpperCase(Locale.ROOT));
        material.setNombre(dto.getNombre().trim());
        material.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            material.setActivo(dto.getActivo());
        }

        return mapToResponseDTO(materialRepository.save(material));
    }

    public List<MaterialResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(materialRepository.findAll());
    }

    public PageResponseDTO<MaterialResponseDTO> obtenerPaginado(int page, String sortBy, String direction) {
        int pageNumber = Math.max(page, 0);
        PageRequest pageable = PageRequest.of(pageNumber, PAGE_SIZE, construirSortMateriales(sortBy, direction));

        Page<MaterialResponseDTO> result = materialRepository.findAll(pageable).map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    public byte[] generarReporteExcel(Boolean activo, String busqueda, String sortBy, String direction) {
        List<MaterialResponseDTO> materiales = mapToResponseDTOList(
                materialRepository.findAll(construirSortMateriales(sortBy, direction)));

        List<MaterialResponseDTO> filtrados = materiales.stream()
                .filter(material -> activo == null || Objects.equals(material.getActivo(), activo))
                .filter(material -> coincideBusqueda(material, busqueda))
                .collect(Collectors.toList());

        String[] headers = {
                "ID", "Codigo", "Nombre", "Descripcion", "Estado", "Fecha Registro", "Fecha Actualizacion"
        };

        return ExcelReportBuilder.generate(
                "Materiales",
                "Reporte de materiales",
                headers,
                filtrados.stream()
                        .map(material -> new Object[] {
                                material.getId() != null ? material.getId() : 0L,
                                nvl(material.getCodigo()),
                                nvl(material.getNombre()),
                                nvl(material.getDescripcion()),
                                Boolean.TRUE.equals(material.getActivo()) ? "Activo" : "Inactivo",
                                material.getFechaRegistro() != null ? material.getFechaRegistro().toString() : "",
                                material.getFechaActualizacion() != null ? material.getFechaActualizacion().toString() : ""
                        })
                        .collect(Collectors.toList()));
    }

    public List<MaterialResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(materialRepository.findByActivo(true));
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
        String codigoNormalizado = codigo.trim().toUpperCase(Locale.ROOT);
        String nombreNormalizado = nombre.trim();

        materialRepository.findByCodigo(codigoNormalizado)
                .filter(material -> !Objects.equals(material.getId(), idActual))
                .ifPresent(material -> {
                    throw new BadRequestException("Ya existe un material con el codigo: " + codigo);
                });

        materialRepository.findByNombre(nombreNormalizado)
                .filter(material -> !Objects.equals(material.getId(), idActual))
                .ifPresent(material -> {
                    throw new BadRequestException("Ya existe un material con el nombre: " + nombre);
                });
    }
}





