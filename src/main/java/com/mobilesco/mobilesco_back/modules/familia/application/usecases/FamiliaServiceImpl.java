/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/application/usecases/FamiliaServiceImpl.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: FamiliaServiceImpl
 * CONTEXTO: Implementacion de casos de uso del modulo; orquesta reglas de negocio y puertos de salida.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.familia.application.usecases;

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

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaCreateDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaUpdateDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.codes.CatalogCodeGenerator;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.familia.application.ports.FamiliaPersistencePort;
import com.mobilesco.mobilesco_back.modules.familia.application.ports.LineaLookupPort;
import com.mobilesco.mobilesco_back.modules.familia.application.ports.ModeloFamiliaValidationPort;

@Service
public class FamiliaServiceImpl implements FamiliaUseCase {

    private static final int PAGE_SIZE = 10;

    private final FamiliaPersistencePort familiaRepository;
    private final LineaLookupPort lineaRepository;
    private final ModeloFamiliaValidationPort modeloRepository;

    public FamiliaServiceImpl(
            FamiliaPersistencePort familiaRepository,
            LineaLookupPort lineaRepository,
            ModeloFamiliaValidationPort modeloRepository) {
        this.familiaRepository = familiaRepository;
        this.lineaRepository = lineaRepository;
        this.modeloRepository = modeloRepository;
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

    public synchronized FamiliaResponseDTO crear(FamiliaCreateDTO dto) {
        LineaModel linea = lineaRepository.findById(dto.getLineaId())
                .orElseThrow(() -> new NotFoundException("Linea no encontrada con ID: " + dto.getLineaId()));
        String nombre = dto.getNombre().trim();
        if (familiaRepository.existsByLineaIdAndNombreIgnoreCase(linea.getId(), nombre)) {
            throw new BadRequestException("Ya existe una familia con el nombre: " + nombre + " en la linea seleccionada");
        }

        FamiliaModel familia = new FamiliaModel();
        familia.setCodigo(sugerirCodigo(nombre, linea.getId()));
        familia.setNombre(nombre);
        familia.setDescripcion(dto.getDescripcion());
        familia.setLinea(linea);
        familia.setActivo(true);

        FamiliaModel guardado = familiaRepository.save(familia);
        return mapToResponseDTO(guardado);
    }

    public String sugerirCodigo(String nombre) {
        return CatalogCodeGenerator.generate(nombre, familiaRepository.findAll().stream()
                .map(FamiliaModel::getCodigo)
                .toList());
    }

    public String sugerirCodigo(String nombre, Long lineaId) {
        if (lineaId == null) {
            return sugerirCodigo(nombre);
        }
        if (!lineaRepository.existsById(lineaId)) {
            throw new NotFoundException("Linea no encontrada con ID: " + lineaId);
        }
        return CatalogCodeGenerator.generate(nombre, familiaRepository.findByLineaId(lineaId).stream()
                .map(FamiliaModel::getCodigo)
                .toList());
    }

    // ========== READ ==========

    private Sort construirSortFamilias(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return TypeSafeSorts.ascWithId(FamiliaModel.class, FamiliaModel::getNombre, FamiliaModel::getId);
        }

        String campo = sortBy.trim();
        String campoNormalizado = campo.toLowerCase(Locale.ROOT);

        switch (campoNormalizado) {
            case "id":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descById(FamiliaModel.class, FamiliaModel::getId)
                        : TypeSafeSorts.ascById(FamiliaModel.class, FamiliaModel::getId);
            case "codigo":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(FamiliaModel.class, FamiliaModel::getCodigo, FamiliaModel::getId)
                        : TypeSafeSorts.ascWithId(FamiliaModel.class, FamiliaModel::getCodigo, FamiliaModel::getId);
            case "nombre":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(FamiliaModel.class, FamiliaModel::getNombre, FamiliaModel::getId)
                        : TypeSafeSorts.ascWithId(FamiliaModel.class, FamiliaModel::getNombre, FamiliaModel::getId);
            case "descripcion":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(FamiliaModel.class, FamiliaModel::getDescripcion, FamiliaModel::getId)
                        : TypeSafeSorts.ascWithId(FamiliaModel.class, FamiliaModel::getDescripcion, FamiliaModel::getId);
            case "activo":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(FamiliaModel.class, FamiliaModel::getActivo, FamiliaModel::getId)
                        : TypeSafeSorts.ascWithId(FamiliaModel.class, FamiliaModel::getActivo, FamiliaModel::getId);
            case "createdat":
            case "created_at":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(FamiliaModel.class, FamiliaModel::getCreatedAt, FamiliaModel::getId)
                        : TypeSafeSorts.ascWithId(FamiliaModel.class, FamiliaModel::getCreatedAt, FamiliaModel::getId);
            default:
                return TypeSafeSorts.ascWithId(FamiliaModel.class, FamiliaModel::getNombre, FamiliaModel::getId);
        }
    }

    public List<FamiliaResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(familiaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean activo, String busqueda, Long lineaId, String sortBy, String direction) {
        List<FamiliaResponseDTO> familias = mapToResponseDTOList(
                familiaRepository.findAll(construirSortFamilias(sortBy, direction)));

        List<FamiliaResponseDTO> filtradas = familias.stream()
                .filter(familia -> activo == null || Objects.equals(familia.getActivo(), activo))
                .filter(familia -> lineaId == null || Objects.equals(familia.getLineaId(), lineaId))
                .filter(familia -> coincideBusqueda(familia, busqueda))
                .collect(Collectors.toList());

        String[] headers = {
                "ID", "Codigo", "Nombre", "Descripcion", "Linea", "Estado", "Creada"
        };

        return ExcelReportBuilder.generate(
                "Familias",
                "Reporte de familias",
                headers,
                filtradas.stream()
                        .map(familia -> new Object[] {
                                familia.getId() != null ? familia.getId() : 0L,
                                nvl(familia.getCodigo()),
                                nvl(familia.getNombre()),
                                nvl(familia.getDescripcion()),
                                nvl(familia.getLineaNombre()),
                                Boolean.TRUE.equals(familia.getActivo()) ? "Activo" : "Inactivo",
                                familia.getCreatedAt() != null ? familia.getCreatedAt().toString() : ""
                        })
                        .collect(Collectors.toList()));
    }

    public PageResponseDTO<FamiliaResponseDTO> obtenerPaginado(
            int page,
            String sortBy,
            String direction,
            Boolean activo,
            String busqueda,
            Long lineaId) {
        int pageNumber = Math.max(page, 0);
        PageRequest pageable = PageRequest.of(pageNumber, PAGE_SIZE, construirSortFamilias(sortBy, direction));

        Page<FamiliaResponseDTO> result = familiaRepository
                .buscarPaginado(activo, normalizarBusqueda(busqueda), lineaId, pageable)
                .map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private String normalizarBusqueda(String busqueda) {
        return busqueda == null || busqueda.isBlank() ? null : busqueda.trim();
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

    private boolean coincideBusqueda(FamiliaResponseDTO familia, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        familia.getId() != null ? String.valueOf(familia.getId()) : null,
                        familia.getCodigo(),
                        familia.getNombre(),
                        familia.getDescripcion(),
                        familia.getLineaNombre(),
                        familia.getLineaId() != null ? String.valueOf(familia.getLineaId()) : null,
                        familia.getActivo() != null ? (familia.getActivo() ? "activo" : "inactivo") : null,
                        familia.getCreatedAt() != null ? familia.getCreatedAt().toString() : null)
                .filter(valor -> valor != null && !valor.isBlank())
                .map(valor -> valor.toLowerCase(Locale.ROOT))
                .anyMatch(valor -> valor.contains(termino));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    // ========== UPDATE ==========

    public FamiliaResponseDTO actualizar(Long id, FamiliaUpdateDTO dto) {

        FamiliaModel existente = familiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + id));

        LineaModel lineaDestino = dto.getLineaId() != null
                ? lineaRepository.findById(dto.getLineaId())
                        .orElseThrow(() -> new NotFoundException("Linea no encontrada con ID: " + dto.getLineaId()))
                : existente.getLinea();
        if (lineaDestino == null || lineaDestino.getId() == null) {
            throw new BadRequestException("La familia debe estar asociada a una linea");
        }

        String codigoDestino = dto.getCodigo() != null ? dto.getCodigo().trim() : existente.getCodigo();
        String nombreDestino = dto.getNombre() != null ? dto.getNombre().trim() : existente.getNombre();

        if (familiaRepository.existsByLineaIdAndCodigoIgnoreCaseAndIdNot(lineaDestino.getId(), codigoDestino, id)) {
            throw new BadRequestException("Ya existe una familia con el codigo: " + codigoDestino + " en la linea seleccionada");
        }

        if (familiaRepository.existsByLineaIdAndNombreIgnoreCaseAndIdNot(lineaDestino.getId(), nombreDestino, id)) {
            throw new BadRequestException("Ya existe una familia con el nombre: " + nombreDestino + " en la linea seleccionada");
        }

        existente.setCodigo(codigoDestino);
        existente.setNombre(nombreDestino);

        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }

        existente.setLinea(lineaDestino);

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        FamiliaModel actualizado = familiaRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }

    public FamiliaResponseDTO activar(Long id) {
        FamiliaModel existente = familiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + id));

        existente.setActivo(true);
        return mapToResponseDTO(familiaRepository.save(existente));
    }

    public FamiliaResponseDTO desactivar(Long id) {
        FamiliaModel existente = familiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + id));

        existente.setActivo(false);
        return mapToResponseDTO(familiaRepository.save(existente));
    }

    // ========== DELETE ==========

    public void eliminar(Long id) {
        if (!familiaRepository.existsById(id)) {
            throw new NotFoundException("Familia no encontrada con ID: " + id);
        }

        if (modeloRepository.existsByFamiliaId(id)) {
            throw new BadRequestException("No se puede eliminar la familia porque tiene modelos asociados");
        }

        familiaRepository.deleteById(id);
    }
}





