/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/modelo/application/usecases/ModeloService.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ModeloService
 * CONTEXTO: Servicio de aplicacion del modulo Modelo.
 * NOTAS: Orquesta reglas de negocio, filtros y gestion de imagenes.
 */
package com.mobilesco.mobilesco_back.modules.modelo.application.usecases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
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
import com.mobilesco.mobilesco_back.modules.shared.application.codes.CatalogCodeGenerator;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.out.persistence.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCreateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCategoriaDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloUpdateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.AlmacenamientoImagenesService;

@Service
public class ModeloService {

    private static final int PAGE_SIZE = 10;

    private final ModeloRepository modeloRepository;
    private final FamiliaRepository familiaRepository;
    private final MaterialRepository materialRepository;
    private final ProductoRepository productoRepository;
    private final NivelRepository nivelRepository;
    private final AlmacenamientoImagenesService almacenamientoImagenesService;

    public ModeloService(ModeloRepository modeloRepository,
                         FamiliaRepository familiaRepository,
                         MaterialRepository materialRepository,
                         ProductoRepository productoRepository,
                         NivelRepository nivelRepository,
                         AlmacenamientoImagenesService almacenamientoImagenesService) {
        this.modeloRepository = modeloRepository;
        this.familiaRepository = familiaRepository;
        this.materialRepository = materialRepository;
        this.productoRepository = productoRepository;
        this.nivelRepository = nivelRepository;
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
        dto.setCategorias(nivelRepository.findByModeloIdOrderByNombreAsc(modelo.getId()).stream()
                .map(this::mapCategoria)
                .toList());
        dto.setMateriales(modelo.getMateriales().stream()
                .sorted((izq, der) -> {
                    String nombreIzq = izq.getNombre() == null ? "" : izq.getNombre().toLowerCase(Locale.ROOT);
                    String nombreDer = der.getNombre() == null ? "" : der.getNombre().toLowerCase(Locale.ROOT);
                    int comparacion = nombreIzq.compareTo(nombreDer);
                    if (comparacion != 0) {
                        return comparacion;
                    }
                    long idIzq = izq.getId() == null ? 0L : izq.getId();
                    long idDer = der.getId() == null ? 0L : der.getId();
                    return Long.compare(idIzq, idDer);
                })
                .map(this::mapMaterial)
                .toList());

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
            return TypeSafeSorts.ascWithId(ModeloModel.class, ModeloModel::getNombre, ModeloModel::getId);
        }

        String campoNormalizado = sortBy.trim().toLowerCase(Locale.ROOT);

        return switch (campoNormalizado) {
            case "id" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descById(ModeloModel.class, ModeloModel::getId)
                    : TypeSafeSorts.ascById(ModeloModel.class, ModeloModel::getId);
            case "codigo" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ModeloModel.class, ModeloModel::getCodigo, ModeloModel::getId)
                    : TypeSafeSorts.ascWithId(ModeloModel.class, ModeloModel::getCodigo, ModeloModel::getId);
            case "nombre" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ModeloModel.class, ModeloModel::getNombre, ModeloModel::getId)
                    : TypeSafeSorts.ascWithId(ModeloModel.class, ModeloModel::getNombre, ModeloModel::getId);
            case "descripcion" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ModeloModel.class, ModeloModel::getDescripcion, ModeloModel::getId)
                    : TypeSafeSorts.ascWithId(ModeloModel.class, ModeloModel::getDescripcion, ModeloModel::getId);
            case "activo" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ModeloModel.class, ModeloModel::getActivo, ModeloModel::getId)
                    : TypeSafeSorts.ascWithId(ModeloModel.class, ModeloModel::getActivo, ModeloModel::getId);
            case "createdat", "created_at" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ModeloModel.class, ModeloModel::getCreatedAt, ModeloModel::getId)
                    : TypeSafeSorts.ascWithId(ModeloModel.class, ModeloModel::getCreatedAt, ModeloModel::getId);
            case "updatedat", "updated_at" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ModeloModel.class, ModeloModel::getUpdatedAt, ModeloModel::getId)
                    : TypeSafeSorts.ascWithId(ModeloModel.class, ModeloModel::getUpdatedAt, ModeloModel::getId);
            case "familia", "familianombre" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descNestedWithId(ModeloModel.class, ModeloModel::getFamilia, FamiliaModel::getNombre, ModeloModel::getId)
                    : TypeSafeSorts.ascNestedWithId(ModeloModel.class, ModeloModel::getFamilia, FamiliaModel::getNombre, ModeloModel::getId);
            default -> TypeSafeSorts.ascWithId(ModeloModel.class, ModeloModel::getNombre, ModeloModel::getId);
        };
    }

    @Transactional
    public synchronized ModeloResponseDTO crear(ModeloCreateDTO dto) {
        FamiliaModel familia = familiaRepository.findById(dto.getFamiliaId())
                .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + dto.getFamiliaId()));
        if (modeloRepository.findAll().stream()
                .anyMatch(item -> item.getNombre().equalsIgnoreCase(dto.getNombre().trim()))) {
            throw new BadRequestException("Ya existe un modelo con el nombre: " + dto.getNombre());
        }

        ModeloModel modelo = new ModeloModel();
        modelo.setCodigo(sugerirCodigo(dto.getNombre()));
        modelo.setNombre(dto.getNombre());
        modelo.setDescripcion(dto.getDescripcion());
        modelo.setUrlImagen(dto.getUrlImagen());
        modelo.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        modelo.setFamilia(familia);

        ModeloModel guardado = modeloRepository.save(modelo);
        sincronizarCategorias(guardado, dto.getCategorias());
        sincronizarMateriales(guardado, dto.getMateriales());
        guardado = modeloRepository.save(guardado);
        return mapToResponseDTO(guardado);
    }

    public String sugerirCodigo(String nombre) {
        return CatalogCodeGenerator.generate(nombre, modeloRepository.findAll().stream()
                .map(ModeloModel::getCodigo)
                .toList());
    }

    @Transactional(readOnly = true)
    public List<ModeloResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(modeloRepository.findAll());
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public ModeloResponseDTO obtenerPorId(Long id) {
        ModeloModel modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modelo no encontrado con ID: " + id));
        return mapToResponseDTO(modelo);
    }

    @Transactional(readOnly = true)
    public List<ModeloResponseDTO> obtenerPorFamilia(Long familiaId) {
        if (!familiaRepository.existsById(familiaId)) {
            throw new NotFoundException("Familia no encontrada con ID: " + familiaId);
        }
        return mapToResponseDTOList(modeloRepository.findByFamiliaId(familiaId));
    }

    @Transactional(readOnly = true)
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

    @Transactional
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
        if (dto.getCategorias() != null) {
            sincronizarCategorias(actualizado, dto.getCategorias());
        }
        if (dto.getMateriales() != null) {
            sincronizarMateriales(actualizado, dto.getMateriales());
        }
        actualizado = modeloRepository.save(actualizado);
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

    @Transactional
    public void eliminar(Long id) {
        if (!modeloRepository.existsById(id)) {
            throw new NotFoundException("Modelo no encontrado con ID: " + id);
        }

        if (productoRepository.existsByModeloId(id)) {
            throw new BadRequestException("No se puede eliminar el modelo porque tiene productos asociados");
        }

        nivelRepository.deleteByModeloId(id);
        modeloRepository.deleteById(id);
    }

    private ModeloCategoriaDTO mapCategoria(NivelModel nivel) {
        ModeloCategoriaDTO dto = new ModeloCategoriaDTO();
        dto.setId(nivel.getId());
        dto.setCodigo(nivel.getCodigo());
        dto.setNombre(nivel.getNombre());
        dto.setDescripcion(nivel.getDescripcion());
        dto.setActivo(nivel.getActivo());
        return dto;
    }

    private MaterialResponseDTO mapMaterial(MaterialModel material) {
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

    private void sincronizarMateriales(ModeloModel modelo, List<Long> materialIds) {
        if (materialIds == null) {
            return;
        }

        Set<MaterialModel> materiales = new LinkedHashSet<>();
        Set<Long> idsUnicos = new HashSet<>();

        for (Long materialId : materialIds) {
            if (materialId == null || !idsUnicos.add(materialId)) {
                continue;
            }

            MaterialModel material = materialRepository.findById(materialId)
                    .orElseThrow(() -> new NotFoundException("Material no encontrado con ID: " + materialId));
            materiales.add(material);
        }

        modelo.getMateriales().clear();
        modelo.getMateriales().addAll(materiales);
    }

    private void sincronizarCategorias(ModeloModel modelo, List<ModeloCategoriaDTO> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            throw new BadRequestException("El modelo debe tener al menos una categoria");
        }

        List<NivelModel> existentes = nivelRepository.findByModeloIdOrderByNombreAsc(modelo.getId());
        Set<Long> idsConservados = new HashSet<>();
        Set<String> nombres = new HashSet<>();
        Set<String> codigos = new HashSet<>();
        List<String> codigosUsados = nivelRepository.findAll().stream()
                .map(NivelModel::getCodigo)
                .collect(Collectors.toCollection(ArrayList::new));

        for (ModeloCategoriaDTO categoria : categorias) {
            String nombre = categoria.getNombre() == null ? "" : categoria.getNombre().trim();
            if (nombre.isBlank()) {
                throw new BadRequestException("Todas las categorias deben tener nombre");
            }
            if (!nombres.add(nombre.toLowerCase(Locale.ROOT))) {
                throw new BadRequestException("No se pueden repetir categorias dentro del mismo modelo: " + nombre);
            }

            NivelModel nivel;
            if (categoria.getId() != null) {
                nivel = existentes.stream()
                        .filter(item -> item.getId().equals(categoria.getId()))
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException("La categoria no pertenece al modelo"));
                idsConservados.add(nivel.getId());
            } else {
                nivel = new NivelModel();
                nivel.setModelo(modelo);
            }
            boolean nombreOcupado = nivelRepository.findByNombre(nombre)
                    .filter(item -> nivel.getId() == null || !item.getId().equals(nivel.getId()))
                    .isPresent();
            if (nombreOcupado) {
                throw new BadRequestException("Ya existe una categoria con el nombre: " + nombre);
            }

            String codigo = categoria.getCodigo() == null ? "" : categoria.getCodigo().trim().toUpperCase(Locale.ROOT);
            if (codigo.isBlank()) {
                codigo = CatalogCodeGenerator.generate(nombre, codigosUsados);
            }
            if (!codigos.add(codigo)) {
                throw new BadRequestException("No se pueden repetir codigos de categoria dentro del mismo modelo: " + codigo);
            }
            boolean codigoOcupado = nivelRepository.existsByCodigo(codigo)
                    && (nivel.getId() == null || !codigo.equalsIgnoreCase(nivel.getCodigo()));
            if (codigoOcupado) {
                throw new BadRequestException("Ya existe una categoria con el codigo: " + codigo);
            }
            codigosUsados.add(codigo);

            nivel.setCodigo(codigo);
            nivel.setNombre(nombre);
            nivel.setDescripcion(categoria.getDescripcion());
            nivel.setActivo(categoria.getActivo() == null || Boolean.TRUE.equals(categoria.getActivo()));
            NivelModel guardado = nivelRepository.save(nivel);
            idsConservados.add(guardado.getId());
        }

        for (NivelModel existente : existentes) {
            if (idsConservados.contains(existente.getId())) {
                continue;
            }
            if (productoRepository.existsByNivelId(existente.getId())) {
                throw new BadRequestException(
                        "No se puede eliminar la categoria " + existente.getNombre() + " porque tiene productos asociados");
            }
            nivelRepository.delete(existente);
        }
    }
}
