/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/modelo/application/usecases/ModeloService.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ModeloService
 * CONTEXTO: Servicio de aplicacion del modulo Modelo.
 * NOTAS: Orquesta reglas de negocio, filtros y gestion de imagenes.
 */
package com.mobilesco.mobilesco_back.modules.modelo.application.usecases;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCreateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloUpdateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.AlmacenamientoImagenesService;

@Service
public class ModeloService {

    private static final int PAGE_SIZE = 10;

    private final ModeloRepository modeloRepository;
    private final FamiliaRepository familiaRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenamientoImagenesService almacenamientoImagenesService;

    public ModeloService(ModeloRepository modeloRepository,
                         FamiliaRepository familiaRepository,
                         ProductoRepository productoRepository,
                         AlmacenamientoImagenesService almacenamientoImagenesService) {
        this.modeloRepository = modeloRepository;
        this.familiaRepository = familiaRepository;
        this.productoRepository = productoRepository;
        this.almacenamientoImagenesService = almacenamientoImagenesService;
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

    private Sort construirSortModelos(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
        }

        String campoNormalizado = sortBy.trim().toLowerCase(Locale.ROOT);

        return switch (campoNormalizado) {
            case "id" -> Sort.by(sortDirection, "id");
            case "codigo" -> Sort.by(sortDirection, "codigo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "nombre" -> Sort.by(sortDirection, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
            case "descripcion" -> Sort.by(sortDirection, "descripcion").and(Sort.by(Sort.Direction.ASC, "id"));
            case "activo" -> Sort.by(sortDirection, "activo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "createdat", "created_at" -> Sort.by(sortDirection, "createdAt").and(Sort.by(Sort.Direction.ASC, "id"));
            case "updatedat", "updated_at" -> Sort.by(sortDirection, "updatedAt").and(Sort.by(Sort.Direction.ASC, "id"));
            case "familia", "familianombre" -> Sort.by(sortDirection, "familia.nombre").and(Sort.by(Sort.Direction.ASC, "id"));
            default -> Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
        };
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

    public List<ModeloResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(modeloRepository.findByActivo(true));
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean activo, String busqueda, String familia, String sortBy, String direction) {
        List<ModeloResponseDTO> modelos = mapToResponseDTOList(
                modeloRepository.findAll(construirSortModelos(sortBy, direction)));

        String familiaNormalizada = familia == null ? "" : familia.trim().toLowerCase(Locale.ROOT);
        List<ModeloResponseDTO> filtrados = modelos.stream()
                .filter(modelo -> activo == null || Objects.equals(modelo.getActivo(), activo))
                .filter(modelo -> familiaNormalizada.isBlank()
                        || (modelo.getFamiliaNombre() != null
                        && modelo.getFamiliaNombre().toLowerCase(Locale.ROOT).contains(familiaNormalizada)))
                .filter(modelo -> coincideBusqueda(modelo, busqueda))
                .collect(Collectors.toList());

        String[] headers = {
                "ID", "Codigo", "Nombre", "Descripcion", "Familia", "Estado", "Creado", "Actualizado"
        };

        return ExcelReportBuilder.generate(
                "Modelos",
                "Reporte de modelos",
                headers,
                filtrados.stream()
                        .map(modelo -> new Object[] {
                                modelo.getId() != null ? modelo.getId() : 0L,
                                nvl(modelo.getCodigo()),
                                nvl(modelo.getNombre()),
                                nvl(modelo.getDescripcion()),
                                nvl(modelo.getFamiliaNombre()),
                                Boolean.TRUE.equals(modelo.getActivo()) ? "Activo" : "Inactivo",
                                modelo.getCreatedAt() != null ? modelo.getCreatedAt().toString() : "",
                                modelo.getUpdatedAt() != null ? modelo.getUpdatedAt().toString() : ""
                        })
                        .collect(Collectors.toList()));
    }

    public PageResponseDTO<ModeloResponseDTO> obtenerPaginado(int page, String sortBy, String direction) {
        int pageNumber = Math.max(page, 0);
        PageRequest pageable = PageRequest.of(pageNumber, PAGE_SIZE, construirSortModelos(sortBy, direction));

        Page<ModeloResponseDTO> result = modeloRepository.findAll(pageable).map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
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

    private boolean coincideBusqueda(ModeloResponseDTO modelo, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        modelo.getId() != null ? String.valueOf(modelo.getId()) : null,
                        modelo.getCodigo(),
                        modelo.getNombre(),
                        modelo.getDescripcion(),
                        modelo.getFamiliaNombre(),
                        modelo.getFamiliaId() != null ? String.valueOf(modelo.getFamiliaId()) : null,
                        modelo.getActivo() != null ? (modelo.getActivo() ? "activo" : "inactivo") : null,
                        modelo.getCreatedAt() != null ? modelo.getCreatedAt().toString() : null,
                        modelo.getUpdatedAt() != null ? modelo.getUpdatedAt().toString() : null)
                .filter(valor -> valor != null && !valor.isBlank())
                .map(valor -> valor.toLowerCase(Locale.ROOT))
                .anyMatch(valor -> valor.contains(termino));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
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

    public ModeloResponseDTO activar(Long id) {
        ModeloModel existente = modeloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modelo no encontrado con ID: " + id));

        existente.setActivo(true);
        return mapToResponseDTO(modeloRepository.save(existente));
    }

    public ModeloResponseDTO desactivar(Long id) {
        ModeloModel existente = modeloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modelo no encontrado con ID: " + id));

        existente.setActivo(false);
        return mapToResponseDTO(modeloRepository.save(existente));
    }

    @Transactional
    public ModeloResponseDTO actualizarImagen(Long id, MultipartFile archivo) {
        ModeloModel existente = modeloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modelo no encontrado con ID: " + id));

        try {
            String urlPublica = almacenamientoImagenesService.guardarImagenModelo(id, archivo);
            existente.setUrlImagen(urlPublica);
            return mapToResponseDTO(modeloRepository.save(existente));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (IOException e) {
            throw new BadRequestException("No se pudo guardar la imagen. Verifica que el archivo sea valido.");
        }
    }

    @Transactional
    public ModeloResponseDTO eliminarImagen(Long id) {
        ModeloModel existente = modeloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modelo no encontrado con ID: " + id));

        existente.setUrlImagen(null);
        return mapToResponseDTO(modeloRepository.save(existente));
    }

    public void eliminar(Long id) {
        if (!modeloRepository.existsById(id)) {
            throw new NotFoundException("Modelo no encontrado con ID: " + id);
        }

        if (productoRepository.existsByModeloId(id)) {
            throw new BadRequestException("No se puede eliminar el modelo porque tiene productos asociados");
        }

        modeloRepository.deleteById(id);
    }
}
