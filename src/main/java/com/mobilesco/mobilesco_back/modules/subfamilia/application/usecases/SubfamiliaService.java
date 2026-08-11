package com.mobilesco.mobilesco_back.modules.subfamilia.application.usecases;

import static org.springframework.data.core.TypedPropertyPath.path;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.codes.CatalogCodeGenerator;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.subfamilia.domain.models.SubfamiliaModel;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.in.api.dtos.SubfamiliaCreateDTO;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.in.api.dtos.SubfamiliaResponseDTO;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.in.api.dtos.SubfamiliaUpdateDTO;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.out.persistence.repositories.SubfamiliaRepository;

@Service
public class SubfamiliaService {

    private static final int PAGE_SIZE = 10;

    private final SubfamiliaRepository subfamiliaRepository;
    private final FamiliaRepository familiaRepository;
    private final ModeloRepository modeloRepository;

    public SubfamiliaService(
            SubfamiliaRepository subfamiliaRepository,
            FamiliaRepository familiaRepository,
            ModeloRepository modeloRepository) {
        this.subfamiliaRepository = subfamiliaRepository;
        this.familiaRepository = familiaRepository;
        this.modeloRepository = modeloRepository;
    }

    @Transactional
    public SubfamiliaResponseDTO crear(SubfamiliaCreateDTO dto) {
        FamiliaModel familia = familiaRepository.findById(dto.getFamiliaId())
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + dto.getFamiliaId()));
        String nombre = normalizarObligatorio(dto.getNombre(), "El nombre es obligatorio");
        String codigo = dto.getCodigo() == null || dto.getCodigo().isBlank()
                ? sugerirCodigo(nombre, familia.getId())
                : dto.getCodigo().trim().toUpperCase(Locale.ROOT);

        validarDuplicados(familia.getId(), codigo, nombre, null);

        SubfamiliaModel subfamilia = new SubfamiliaModel();
        subfamilia.setCodigo(codigo);
        subfamilia.setNombre(nombre);
        subfamilia.setDescripcion(dto.getDescripcion());
        subfamilia.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        subfamilia.setFamilia(familia);

        return mapToResponseDTO(subfamiliaRepository.save(subfamilia));
    }

    @Transactional(readOnly = true)
    public String sugerirCodigo(String nombre, Long familiaId) {
        if (familiaId == null) {
            return CatalogCodeGenerator.generate(nombre, subfamiliaRepository.findAll().stream()
                    .map(SubfamiliaModel::getCodigo)
                    .toList());
        }
        if (!familiaRepository.existsById(familiaId)) {
            throw new NotFoundException("Familia no encontrada con ID: " + familiaId);
        }
        return CatalogCodeGenerator.generate(nombre, subfamiliaRepository.findByFamiliaId(familiaId).stream()
                .map(SubfamiliaModel::getCodigo)
                .toList());
    }

    @Transactional(readOnly = true)
    public List<SubfamiliaResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(subfamiliaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<SubfamiliaResponseDTO> obtenerPaginado(
            int page,
            String sortBy,
            String direction,
            Boolean activo,
            String busqueda,
            Long familiaId) {
        Sort sort = construirSort(sortBy, direction);
        Page<SubfamiliaResponseDTO> result = subfamiliaRepository
                .buscarPaginado(activo, normalizarBusqueda(busqueda), familiaId, PageRequest.of(Math.max(page, 0), PAGE_SIZE, sort))
                .map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<SubfamiliaResponseDTO> obtenerActivas() {
        return mapToResponseDTOList(subfamiliaRepository.findByActivo(true));
    }

    @Transactional(readOnly = true)
    public SubfamiliaResponseDTO obtenerPorId(Long id) {
        return mapToResponseDTO(subfamiliaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subfamilia no encontrada con ID: " + id)));
    }

    @Transactional(readOnly = true)
    public List<SubfamiliaResponseDTO> obtenerPorFamilia(Long familiaId) {
        if (!familiaRepository.existsById(familiaId)) {
            throw new NotFoundException("Familia no encontrada con ID: " + familiaId);
        }
        return mapToResponseDTOList(subfamiliaRepository.findByFamiliaId(familiaId));
    }

    @Transactional(readOnly = true)
    public List<SubfamiliaResponseDTO> obtenerPorFamiliaYActivo(Long familiaId, Boolean activo) {
        if (!familiaRepository.existsById(familiaId)) {
            throw new NotFoundException("Familia no encontrada con ID: " + familiaId);
        }
        return mapToResponseDTOList(subfamiliaRepository.findByFamiliaIdAndActivo(familiaId, activo));
    }

    @Transactional
    public SubfamiliaResponseDTO actualizar(Long id, SubfamiliaUpdateDTO dto) {
        SubfamiliaModel existente = subfamiliaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subfamilia no encontrada con ID: " + id));
        FamiliaModel familiaDestino = dto.getFamiliaId() != null
                ? familiaRepository.findById(dto.getFamiliaId())
                        .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + dto.getFamiliaId()))
                : existente.getFamilia();

        String codigo = dto.getCodigo() != null ? dto.getCodigo().trim().toUpperCase(Locale.ROOT) : existente.getCodigo();
        String nombre = dto.getNombre() != null ? normalizarObligatorio(dto.getNombre(), "El nombre es obligatorio") : existente.getNombre();
        validarDuplicados(familiaDestino.getId(), codigo, nombre, id);

        existente.setCodigo(codigo);
        existente.setNombre(nombre);
        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }
        existente.setFamilia(familiaDestino);
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }
        return mapToResponseDTO(subfamiliaRepository.save(existente));
    }

    @Transactional
    public SubfamiliaResponseDTO activar(Long id) {
        SubfamiliaModel existente = subfamiliaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subfamilia no encontrada con ID: " + id));
        existente.setActivo(true);
        return mapToResponseDTO(subfamiliaRepository.save(existente));
    }

    @Transactional
    public SubfamiliaResponseDTO desactivar(Long id) {
        SubfamiliaModel existente = subfamiliaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subfamilia no encontrada con ID: " + id));
        existente.setActivo(false);
        return mapToResponseDTO(subfamiliaRepository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!subfamiliaRepository.existsById(id)) {
            throw new NotFoundException("Subfamilia no encontrada con ID: " + id);
        }
        if (modeloRepository.existsBySubfamiliaId(id)) {
            throw new BadRequestException("No se puede eliminar la subfamilia porque tiene modelos asociados");
        }
        subfamiliaRepository.deleteById(id);
    }

    private void validarDuplicados(Long familiaId, String codigo, String nombre, Long idActual) {
        boolean codigoDuplicado = idActual == null
                ? subfamiliaRepository.existsByFamiliaIdAndCodigoIgnoreCase(familiaId, codigo)
                : subfamiliaRepository.existsByFamiliaIdAndCodigoIgnoreCaseAndIdNot(familiaId, codigo, idActual);
        if (codigoDuplicado) {
            throw new BadRequestException("Ya existe una subfamilia con el codigo: " + codigo + " en la familia seleccionada");
        }

        boolean nombreDuplicado = idActual == null
                ? subfamiliaRepository.existsByFamiliaIdAndNombreIgnoreCase(familiaId, nombre)
                : subfamiliaRepository.existsByFamiliaIdAndNombreIgnoreCaseAndIdNot(familiaId, nombre, idActual);
        if (nombreDuplicado) {
            throw new BadRequestException("Ya existe una subfamilia con el nombre: " + nombre + " en la familia seleccionada");
        }
    }

    private Sort construirSort(String sortBy, String direction) {
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String campo = sortBy == null || sortBy.isBlank() ? "nombre" : sortBy.trim();
        if (!List.of("id", "codigo", "nombre", "activo", "createdAt").contains(campo)) {
            campo = "nombre";
        }
        Sort principal = switch (campo) {
            case "id" -> Sort.by(dir, path(SubfamiliaModel::getId));
            case "codigo" -> Sort.by(dir, path(SubfamiliaModel::getCodigo));
            case "activo" -> Sort.by(dir, path(SubfamiliaModel::getActivo));
            case "createdAt" -> Sort.by(dir, path(SubfamiliaModel::getCreatedAt));
            default -> Sort.by(dir, path(SubfamiliaModel::getNombre));
        };
        return principal.and(Sort.by(Sort.Direction.ASC, path(SubfamiliaModel::getId)));
    }

    private String normalizarBusqueda(String busqueda) {
        return busqueda == null || busqueda.isBlank() ? null : busqueda.trim();
    }

    private String normalizarObligatorio(String valor, String mensaje) {
        String normalizado = valor == null ? "" : valor.trim();
        if (normalizado.isBlank()) {
            throw new BadRequestException(mensaje);
        }
        return normalizado;
    }

    private List<SubfamiliaResponseDTO> mapToResponseDTOList(List<SubfamiliaModel> subfamilias) {
        return subfamilias.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    private SubfamiliaResponseDTO mapToResponseDTO(SubfamiliaModel subfamilia) {
        SubfamiliaResponseDTO dto = new SubfamiliaResponseDTO();
        dto.setId(subfamilia.getId());
        dto.setCodigo(subfamilia.getCodigo());
        dto.setNombre(subfamilia.getNombre());
        dto.setDescripcion(subfamilia.getDescripcion());
        dto.setActivo(subfamilia.getActivo());
        dto.setCreatedAt(subfamilia.getCreatedAt());
        if (subfamilia.getFamilia() != null) {
            dto.setFamiliaId(subfamilia.getFamilia().getId());
            dto.setFamiliaNombre(subfamilia.getFamilia().getNombre());
            if (subfamilia.getFamilia().getLinea() != null) {
                dto.setLineaId(subfamilia.getFamilia().getLinea().getId());
                dto.setLineaNombre(subfamilia.getFamilia().getLinea().getNombre());
            }
        }
        return dto;
    }
}
