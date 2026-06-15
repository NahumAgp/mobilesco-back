/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/linea/application/usecases/LineaService.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaService
 * CONTEXTO: Servicio de aplicacion del modulo Linea.
 * NOTAS: Centraliza reglas de negocio y mapeos DTO del catalogo.
 */
package com.mobilesco.mobilesco_back.modules.linea.application.usecases;

import java.util.List;
import java.util.Objects;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;
import com.mobilesco.mobilesco_back.modules.shared.application.codes.CatalogCodeGenerator;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaCreateDTO;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaResponseDTO;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaUpdateDTO;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.out.persistence.repositories.LineaRepository;

@Service
public class LineaService {

    private static final int PAGE_SIZE = 10;
    private final LineaRepository lineaRepository;
    private final FamiliaRepository familiaRepository;

    public LineaService(LineaRepository lineaRepository, FamiliaRepository familiaRepository) {
        this.lineaRepository = lineaRepository;
        this.familiaRepository = familiaRepository;
    }

    // ========== MAPPER ==========

    private LineaResponseDTO mapToResponseDTO(LineaModel linea) {
        LineaResponseDTO dto = new LineaResponseDTO();
        dto.setId(linea.getId());
        dto.setCodigo(linea.getCodigo());
        dto.setNombre(linea.getNombre());
        dto.setDescripcion(linea.getDescripcion());
        dto.setOrden(linea.getOrden());
        dto.setActivo(linea.getActivo());
        dto.setCreatedAt(linea.getCreatedAt());
        return dto;
    }

    private List<LineaResponseDTO> mapToResponseDTOList(List<LineaModel> lineas) {
        return lineas.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== CREATE ==========

    public synchronized LineaResponseDTO crear(LineaCreateDTO dto) {
        if (lineaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new BadRequestException("Ya existe una linea con el nombre: " + dto.getNombre());
        }

        LineaModel linea = new LineaModel();
        linea.setCodigo(generarCodigoDisponible(dto.getNombre()));
        linea.setNombre(dto.getNombre());
        linea.setDescripcion(dto.getDescripcion());
        linea.setOrden(Objects.requireNonNullElse(dto.getOrden(), 0));
        linea.setActivo(true);

        LineaModel guardado = lineaRepository.save(linea);
        return mapToResponseDTO(guardado);
    }

    public String sugerirCodigo(String nombre) {
        return generarCodigoDisponible(nombre);
    }

    private String generarCodigoDisponible(String nombre) {
        return CatalogCodeGenerator.generate(nombre, lineaRepository.findAllCodigos());
    }

    // ========== READ ==========

    private Sort construirSortLineas(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return TypeSafeSorts.ascWithId(LineaModel.class, LineaModel::getOrden, LineaModel::getId);
        }

        String campoNormalizado = sortBy.trim().toLowerCase(Locale.ROOT);

        switch (campoNormalizado) {
            case "id":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descById(LineaModel.class, LineaModel::getId)
                        : TypeSafeSorts.ascById(LineaModel.class, LineaModel::getId);
            case "codigo":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(LineaModel.class, LineaModel::getCodigo, LineaModel::getId)
                        : TypeSafeSorts.ascWithId(LineaModel.class, LineaModel::getCodigo, LineaModel::getId);
            case "nombre":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(LineaModel.class, LineaModel::getNombre, LineaModel::getId)
                        : TypeSafeSorts.ascWithId(LineaModel.class, LineaModel::getNombre, LineaModel::getId);
            case "descripcion":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(LineaModel.class, LineaModel::getDescripcion, LineaModel::getId)
                        : TypeSafeSorts.ascWithId(LineaModel.class, LineaModel::getDescripcion, LineaModel::getId);
            case "orden":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(LineaModel.class, LineaModel::getOrden, LineaModel::getId)
                        : TypeSafeSorts.ascWithId(LineaModel.class, LineaModel::getOrden, LineaModel::getId);
            case "activo":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(LineaModel.class, LineaModel::getActivo, LineaModel::getId)
                        : TypeSafeSorts.ascWithId(LineaModel.class, LineaModel::getActivo, LineaModel::getId);
            case "createdat":
            case "created_at":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(LineaModel.class, LineaModel::getCreatedAt, LineaModel::getId)
                        : TypeSafeSorts.ascWithId(LineaModel.class, LineaModel::getCreatedAt, LineaModel::getId);
            default:
                return TypeSafeSorts.ascWithId(LineaModel.class, LineaModel::getOrden, LineaModel::getId);
        }
    }

    public List<LineaResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(lineaRepository.findAll());
    }

    public byte[] generarReporteExcel(Boolean activo, String busqueda, String sortBy, String direction) {
        List<LineaResponseDTO> lineas = mapToResponseDTOList(
                lineaRepository.findAll(construirSortLineas(sortBy, direction)));

        List<LineaResponseDTO> filtradas = lineas.stream()
                .filter(linea -> activo == null || Objects.equals(linea.getActivo(), activo))
                .filter(linea -> coincideBusqueda(linea, busqueda))
                .collect(Collectors.toList());

        String[] headers = {
                "ID", "Codigo", "Nombre", "Descripcion", "Orden", "Estado", "Creada"
        };

        return ExcelReportBuilder.generate(
                "Lineas",
                "Reporte de lineas de producto",
                headers,
                filtradas.stream()
                        .map(linea -> new Object[] {
                                linea.getId() != null ? linea.getId() : 0L,
                                nvl(linea.getCodigo()),
                                nvl(linea.getNombre()),
                                nvl(linea.getDescripcion()),
                                linea.getOrden() != null ? linea.getOrden() : 0,
                                Boolean.TRUE.equals(linea.getActivo()) ? "Activo" : "Inactivo",
                                linea.getCreatedAt() != null ? linea.getCreatedAt().toString() : ""
                        })
                        .collect(Collectors.toList()));
    }

    private boolean coincideBusqueda(LineaResponseDTO linea, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        linea.getId() != null ? String.valueOf(linea.getId()) : null,
                        linea.getCodigo(),
                        linea.getNombre(),
                        linea.getDescripcion(),
                        linea.getOrden() != null ? String.valueOf(linea.getOrden()) : null,
                        linea.getActivo() != null ? (linea.getActivo() ? "activo" : "inactivo") : null,
                        linea.getCreatedAt() != null ? linea.getCreatedAt().toString() : null)
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(termino));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private LineaResponseDTO cambiarEstado(Long id, boolean activo) {
        LineaModel linea = lineaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Linea no encontrada con ID: " + id));

        linea.setActivo(activo);
        return mapToResponseDTO(lineaRepository.save(linea));
    }

    public PageResponseDTO<LineaResponseDTO> obtenerPaginado(int page, String sortBy, String direction) {
        int pageNumber = Math.max(page, 0);
        PageRequest pageable = PageRequest.of(pageNumber, PAGE_SIZE, construirSortLineas(sortBy, direction));

        Page<LineaResponseDTO> result = lineaRepository.findAll(pageable).map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public List<LineaResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(lineaRepository.findByActivo(true));
    }

    public LineaResponseDTO obtenerPorId(Long id) {
        LineaModel linea = lineaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Linea no encontrada con ID: " + id));
        return mapToResponseDTO(linea);
    }

    // ========== UPDATE ==========

    public LineaResponseDTO actualizar(Long id, LineaUpdateDTO dto) {

        LineaModel existente = lineaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Linea no encontrada con ID: " + id));

        if (dto.getCodigo() != null && !dto.getCodigo().equals(existente.getCodigo())) {
            if (lineaRepository.existsByCodigoIgnoreCaseAndIdNot(dto.getCodigo(), id)) {
                throw new BadRequestException("Ya existe una linea con el codigo: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo());
        }

        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (lineaRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre(), id)) {
                throw new BadRequestException("Ya existe una linea con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre());
        }

        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }

        if (dto.getOrden() != null) {
            existente.setOrden(dto.getOrden());
        }

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        LineaModel actualizado = lineaRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }

    public LineaResponseDTO activar(Long id) {
        return cambiarEstado(id, true);
    }

    public LineaResponseDTO desactivar(Long id) {
        return cambiarEstado(id, false);
    }

    // ========== DELETE ==========

    public void eliminar(Long id) {
        if (!lineaRepository.existsById(id)) {
            throw new NotFoundException("Linea no encontrada con ID: " + id);
        }

        if (familiaRepository.existsByLineaId(id)) {
            throw new BadRequestException("No se puede eliminar la linea porque tiene familias asociadas");
        }

        lineaRepository.deleteById(id);
    }
}
