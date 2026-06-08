package com.mobilesco.mobilesco_back.modules.nivel.application.usecases;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelCreateDTO;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelResponseDTO;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelUpdateDTO;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.codes.CatalogCodeGenerator;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;

@Service
public class NivelService {

    private final NivelRepository nivelRepository;
    private final ProductoRepository productoRepository;
    private final ModeloRepository modeloRepository;

    public NivelService(NivelRepository nivelRepository,
                        ProductoRepository productoRepository,
                        ModeloRepository modeloRepository) {
        this.nivelRepository = nivelRepository;
        this.productoRepository = productoRepository;
        this.modeloRepository = modeloRepository;
    }

    @Transactional
    public synchronized NivelResponseDTO crear(NivelCreateDTO dto) {
        ModeloModel modelo = obtenerModelo(dto.getModeloId());

        if (nivelRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe una categoria con el nombre: " + dto.getNombre());
        }

        NivelModel nivel = new NivelModel();
        nivel.setCodigo(sugerirCodigo(dto.getNombre(), modelo.getId()));
        nivel.setNombre(dto.getNombre().trim());
        nivel.setDescripcion(dto.getDescripcion());
        nivel.setActivo(true);
        nivel.setModelo(modelo);

        return mapToResponseDTO(nivelRepository.save(nivel));
    }

    public String sugerirCodigo(String nombre, Long modeloId) {
        obtenerModelo(modeloId);
        return CatalogCodeGenerator.generate(nombre, nivelRepository.findAll().stream()
                .map(NivelModel::getCodigo)
                .toList());
    }

    @Transactional(readOnly = true)
    public List<NivelResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(nivelRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<NivelResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(nivelRepository.findByActivo(true));
    }

    @Transactional(readOnly = true)
    public List<NivelResponseDTO> obtenerPorModelo(Long modeloId, boolean soloActivos) {
        obtenerModelo(modeloId);
        return mapToResponseDTOList(soloActivos
                ? nivelRepository.findByModeloIdAndActivoTrueOrderByNombreAsc(modeloId)
                : nivelRepository.findByModeloIdOrderByNombreAsc(modeloId));
    }

    @Transactional(readOnly = true)
    public NivelResponseDTO obtenerPorId(Long id) {
        return mapToResponseDTO(obtenerNivel(id));
    }

    @Transactional(readOnly = true)
    public NivelResponseDTO obtenerPorCodigo(String codigo) {
        NivelModel nivel = nivelRepository.findAll().stream()
                .filter(item -> item.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con codigo: " + codigo));
        return mapToResponseDTO(nivel);
    }

    @Transactional(readOnly = true)
    public NivelResponseDTO obtenerPorNombre(String nombre) {
        NivelModel nivel = nivelRepository.findAll().stream()
                .filter(item -> item.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con nombre: " + nombre));
        return mapToResponseDTO(nivel);
    }

    @Transactional
    public NivelResponseDTO activar(Long id) {
        NivelModel nivel = obtenerNivel(id);
        nivel.setActivo(true);
        return mapToResponseDTO(nivelRepository.save(nivel));
    }

    @Transactional
    public NivelResponseDTO desactivar(Long id) {
        NivelModel nivel = obtenerNivel(id);
        if (Boolean.TRUE.equals(nivel.getActivo())
                && nivelRepository.countByModeloIdAndActivoTrue(nivel.getModelo().getId()) <= 1) {
            throw new BadRequestException("El modelo debe conservar al menos una categoria activa");
        }
        nivel.setActivo(false);
        return mapToResponseDTO(nivelRepository.save(nivel));
    }

    @Transactional
    public NivelResponseDTO actualizar(Long id, NivelUpdateDTO dto) {
        NivelModel existente = obtenerNivel(id);
        Long modeloId = existente.getModelo().getId();

        if (dto.getCodigo() != null && !dto.getCodigo().equalsIgnoreCase(existente.getCodigo())) {
            if (nivelRepository.existsByCodigo(dto.getCodigo())) {
                throw new BadRequestException("Ya existe una categoria con el codigo: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo().trim().toUpperCase(Locale.ROOT));
        }

        if (dto.getNombre() != null && !dto.getNombre().equalsIgnoreCase(existente.getNombre())) {
            if (nivelRepository.existsByModeloIdAndNombreIgnoreCaseAndIdNot(modeloId, dto.getNombre(), id)) {
                throw new BadRequestException("El modelo ya tiene una categoria con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre().trim());
        }

        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }

        if (dto.getActivo() != null) {
            if (Boolean.FALSE.equals(dto.getActivo())
                    && Boolean.TRUE.equals(existente.getActivo())
                    && nivelRepository.countByModeloIdAndActivoTrue(modeloId) <= 1) {
                throw new BadRequestException("El modelo debe conservar al menos una categoria activa");
            }
            existente.setActivo(dto.getActivo());
        }

        return mapToResponseDTO(nivelRepository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        NivelModel nivel = obtenerNivel(id);

        if (productoRepository.existsByNivelId(id)) {
            throw new BadRequestException("No se puede eliminar la categoria porque tiene productos asociados");
        }
        if (nivelRepository.countByModeloId(nivel.getModelo().getId()) <= 1) {
            throw new BadRequestException("El modelo debe conservar al menos una categoria");
        }

        nivelRepository.delete(nivel);
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean activo, String busqueda, String sortBy, String direction) {
        List<NivelResponseDTO> filtrados = nivelRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .filter(nivel -> activo == null || Objects.equals(nivel.getActivo(), activo))
                .filter(nivel -> coincideBusqueda(nivel, busqueda))
                .collect(Collectors.toList());

        String[] headers = {
                "ID", "Codigo", "Nombre", "Modelo", "Descripcion", "Estado", "Creado"
        };

        return ExcelReportBuilder.generate(
                "Categorias",
                "Reporte de categorias por modelo",
                headers,
                filtrados.stream()
                        .map(nivel -> new Object[] {
                                nivel.getId() != null ? nivel.getId() : 0L,
                                nivel.getCodigo() == null ? "" : nivel.getCodigo(),
                                nivel.getNombre() == null ? "" : nivel.getNombre(),
                                nivel.getModeloNombre() == null ? "" : nivel.getModeloNombre(),
                                nivel.getDescripcion() == null ? "" : nivel.getDescripcion(),
                                Boolean.TRUE.equals(nivel.getActivo()) ? "Activo" : "Inactivo",
                                nivel.getCreatedAt() != null ? nivel.getCreatedAt().toString() : ""
                        })
                        .collect(Collectors.toList()));
    }

    private ModeloModel obtenerModelo(Long modeloId) {
        return modeloRepository.findById(modeloId)
                .orElseThrow(() -> new NotFoundException("Modelo no encontrado con ID: " + modeloId));
    }

    private NivelModel obtenerNivel(Long id) {
        return nivelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con ID: " + id));
    }

    private NivelResponseDTO mapToResponseDTO(NivelModel nivel) {
        NivelResponseDTO dto = new NivelResponseDTO();
        dto.setId(nivel.getId());
        dto.setCodigo(nivel.getCodigo());
        dto.setNombre(nivel.getNombre());
        dto.setDescripcion(nivel.getDescripcion());
        dto.setActivo(nivel.getActivo());
        dto.setCreatedAt(nivel.getCreatedAt());
        if (nivel.getModelo() != null) {
            dto.setModeloId(nivel.getModelo().getId());
            dto.setModeloNombre(nivel.getModelo().getNombre());
        }
        return dto;
    }

    private List<NivelResponseDTO> mapToResponseDTOList(List<NivelModel> niveles) {
        return niveles.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    private boolean coincideBusqueda(NivelResponseDTO nivel, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        nivel.getId() != null ? String.valueOf(nivel.getId()) : null,
                        nivel.getCodigo(),
                        nivel.getNombre(),
                        nivel.getModeloNombre(),
                        nivel.getDescripcion())
                .filter(valor -> valor != null && !valor.isBlank())
                .map(valor -> valor.toLowerCase(Locale.ROOT))
                .anyMatch(valor -> valor.contains(termino));
    }

}
