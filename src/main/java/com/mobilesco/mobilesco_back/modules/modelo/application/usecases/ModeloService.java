/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/modelo/application/usecases/ModeloService.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ModeloService
 * CONTEXTO: Servicio de aplicacion del modulo Modelo.
 * NOTAS: Orquesta reglas de negocio, filtros y gestion de imagenes.
 */
package com.mobilesco.mobilesco_back.modules.modelo.application.usecases;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloInsumoDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloOperacionDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloUpdateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.SincronizacionInsumosVariantesResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.subfamilia.domain.models.SubfamiliaModel;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.out.persistence.repositories.SubfamiliaRepository;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import com.mobilesco.mobilesco_back.modules.operacion.infrastructure.out.persistence.repositories.OperacionRepository;
import com.mobilesco.mobilesco_back.modules.categoria.domain.models.CategoriaModel;
import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.out.persistence.repositories.CategoriaRepository;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelInsumoModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelOperacionModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelInsumoRepository;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelOperacionRepository;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.producto.application.usecases.ProductoPlantillaModeloService;
import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.AlmacenamientoImagenesService;

@Service
public class ModeloService {

    private static final int PAGE_SIZE = 10;
    private static final String OBSERVACION_HEREDADO_CATEGORIA = "Heredado de la categoria del modelo";

    private record PlantillaInsumoKey(Long materialId, Long insumoId) {
    }

    private final ModeloRepository modeloRepository;
    private final FamiliaRepository familiaRepository;
    private final MaterialRepository materialRepository;
    private final SubfamiliaRepository subfamiliaRepository;
    private final ProductoRepository productoRepository;
    private final NivelRepository nivelRepository;
    private final CategoriaRepository categoriaRepository;
    private final AlmacenamientoImagenesService almacenamientoImagenesService;
    private final InsumoRepository insumoRepository;
    private final OperacionRepository operacionRepository;
    private final NivelInsumoRepository nivelInsumoRepository;
    private final NivelOperacionRepository nivelOperacionRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoPlantillaModeloService productoPlantillaModeloService;

    public ModeloService(ModeloRepository modeloRepository,
                         FamiliaRepository familiaRepository,
                         MaterialRepository materialRepository,
                         SubfamiliaRepository subfamiliaRepository,
                         ProductoRepository productoRepository,
                         NivelRepository nivelRepository,
                         CategoriaRepository categoriaRepository,
                         AlmacenamientoImagenesService almacenamientoImagenesService,
                         InsumoRepository insumoRepository,
                         OperacionRepository operacionRepository,
                         NivelInsumoRepository nivelInsumoRepository,
                         NivelOperacionRepository nivelOperacionRepository,
                         ProductoInsumoRepository productoInsumoRepository,
                         ProductoPlantillaModeloService productoPlantillaModeloService) {
        this.modeloRepository = modeloRepository;
        this.familiaRepository = familiaRepository;
        this.materialRepository = materialRepository;
        this.subfamiliaRepository = subfamiliaRepository;
        this.productoRepository = productoRepository;
        this.nivelRepository = nivelRepository;
        this.categoriaRepository = categoriaRepository;
        this.almacenamientoImagenesService = almacenamientoImagenesService;
        this.insumoRepository = insumoRepository;
        this.operacionRepository = operacionRepository;
        this.nivelInsumoRepository = nivelInsumoRepository;
        this.nivelOperacionRepository = nivelOperacionRepository;
        this.productoInsumoRepository = productoInsumoRepository;
        this.productoPlantillaModeloService = productoPlantillaModeloService;
    }

    private ModeloResponseDTO mapToResponseDTO(ModeloModel modelo) {
        ModeloResponseDTO dto = new ModeloResponseDTO();
        dto.setId(modelo.getId());
        dto.setCodigo(modelo.getCodigo());
        dto.setNombre(modelo.getNombre());
        dto.setDescripcion(modelo.getDescripcion());
        dto.setDescripcionCorta(modelo.getDescripcionCorta());
        dto.setUrlImagen(modelo.getUrlImagen());
        dto.setActivo(modelo.getActivo());
        dto.setCreatedAt(modelo.getCreatedAt());
        dto.setUpdatedAt(modelo.getUpdatedAt());

        if (modelo.getFamilia() != null) {
            dto.setFamiliaId(modelo.getFamilia().getId());
            dto.setFamiliaNombre(modelo.getFamilia().getNombre());
            if (modelo.getFamilia().getLinea() != null) {
                dto.setLineaId(modelo.getFamilia().getLinea().getId());
                dto.setLineaNombre(modelo.getFamilia().getLinea().getNombre());
            }
        }
        if (modelo.getSubfamilia() != null) {
            dto.setSubfamiliaId(modelo.getSubfamilia().getId());
            dto.setSubfamiliaNombre(modelo.getSubfamilia().getNombre());
            dto.setSubfamiliaCodigo(modelo.getSubfamilia().getCodigo());
        }
        dto.setCategorias(nivelRepository.findByModeloIdOrderByCodigoAsc(modelo.getId()).stream()
                .map(this::mapCategoria)
                .toList());
        dto.setMateriales(resolverMaterialesDelModelo(modelo).stream()
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
        dto.setInsumos(List.of());
        dto.setOperaciones(List.of());

        return dto;
    }

    private List<MaterialModel> resolverMaterialesDelModelo(ModeloModel modelo) {
        Map<Long, MaterialModel> materiales = new LinkedHashMap<>();
        modelo.getMateriales().forEach(material -> {
            if (material != null && material.getId() != null) {
                materiales.put(material.getId(), material);
            }
        });

        if (modelo.getId() != null) {
            productoRepository.findMaterialesByModeloId(modelo.getId()).forEach(material -> {
                if (material != null && material.getId() != null) {
                    materiales.putIfAbsent(material.getId(), material);
                }
            });
        }

        return List.copyOf(materiales.values());
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
        SubfamiliaModel subfamilia = resolverSubfamilia(dto.getSubfamiliaId(), familia);
        String nombre = dto.getNombre().trim();
        boolean nombreDuplicado = existeNombreEnClasificacion(familia.getId(), subfamilia, nombre, null);
        if (nombreDuplicado) {
            throw new BadRequestException("Ya existe un modelo con el nombre: " + nombre + " en la clasificación seleccionada");
        }

        ModeloModel modelo = new ModeloModel();
        modelo.setCodigo(sugerirCodigo(nombre, familia.getId(), dto.getSubfamiliaId()));
        modelo.setNombre(nombre);
        modelo.setDescripcion(dto.getDescripcion());
        modelo.setDescripcionCorta(dto.getDescripcionCorta());
        modelo.setUrlImagen(dto.getUrlImagen());
        modelo.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        modelo.setFamilia(familia);
        modelo.setSubfamilia(subfamilia);

        ModeloModel guardado = modeloRepository.save(modelo);
        sincronizarMateriales(guardado, dto.getMateriales());
        sincronizarCategorias(guardado, dto.getCategorias());
        limpiarPlantillaGlobal(guardado);
        guardado = modeloRepository.save(guardado);
        return mapToResponseDTO(guardado);
    }

    public String sugerirCodigo(String nombre) {
        return CatalogCodeGenerator.generate(nombre, modeloRepository.findAll().stream()
                .map(ModeloModel::getCodigo)
                .toList());
    }

    public String sugerirCodigo(String nombre, Long familiaId) {
        return sugerirCodigo(nombre, familiaId, null);
    }

    public String sugerirCodigo(String nombre, Long familiaId, Long subfamiliaId) {
        if (familiaId == null) {
            return sugerirCodigo(nombre);
        }
        if (!familiaRepository.existsById(familiaId)) {
            throw new NotFoundException("Familia no encontrada con ID: " + familiaId);
        }
        List<ModeloModel> modelos = subfamiliaId != null
                ? modeloRepository.findBySubfamiliaId(subfamiliaId)
                : modeloRepository.findByFamiliaIdAndSubfamiliaIsNull(familiaId);
        return CatalogCodeGenerator.generate(nombre, modelos.stream()
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
    public byte[] generarReporteExcel(Boolean activo, String busqueda, String familia, Long familiaId, Long lineaId, String sortBy, String direction) {
        List<ModeloResponseDTO> modelos = mapToResponseDTOList(
                modeloRepository.findAll(construirSortModelos(sortBy, direction)));

        String familiaNormalizada = familia == null ? "" : familia.trim().toLowerCase(Locale.ROOT);
        List<ModeloResponseDTO> filtrados = modelos.stream()
                .filter(modelo -> activo == null || Objects.equals(modelo.getActivo(), activo))
                .filter(modelo -> familiaId == null || Objects.equals(modelo.getFamiliaId(), familiaId))
                .filter(modelo -> lineaId == null || Objects.equals(modelo.getLineaId(), lineaId))
                .filter(modelo -> familiaNormalizada.isBlank()
                        || Stream.of(modelo.getLineaNombre(), modelo.getFamiliaNombre(), modelo.getSubfamiliaNombre())
                        .filter(valor -> valor != null && !valor.isBlank())
                        .map(valor -> valor.toLowerCase(Locale.ROOT))
                        .anyMatch(valor -> valor.contains(familiaNormalizada)))
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
    public PageResponseDTO<ModeloResponseDTO> obtenerPaginado(
            int page,
            String sortBy,
            String direction,
            Boolean activo,
            String busqueda,
            Long familiaId,
            Long lineaId) {
        int pageNumber = Math.max(page, 0);
        PageRequest pageable = PageRequest.of(pageNumber, PAGE_SIZE, construirSortModelos(sortBy, direction));

        Page<ModeloResponseDTO> result = modeloRepository
                .buscarPaginado(activo, normalizarBusqueda(busqueda), familiaId, lineaId, pageable)
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
                        modelo.getLineaNombre(),
                        modelo.getFamiliaNombre(),
                        modelo.getSubfamiliaNombre(),
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

        FamiliaModel familiaDestino = dto.getFamiliaId() != null
                ? familiaRepository.findById(dto.getFamiliaId())
                        .orElseThrow(() -> new NotFoundException("Familia no encontrada con ID: " + dto.getFamiliaId()))
                : existente.getFamilia();
        if (familiaDestino == null || familiaDestino.getId() == null) {
            throw new BadRequestException("El modelo debe estar asociado a una familia");
        }
        String codigoDestino = dto.getCodigo() != null ? dto.getCodigo().trim() : existente.getCodigo();
        String nombreDestino = dto.getNombre() != null ? dto.getNombre().trim() : existente.getNombre();

        SubfamiliaModel subfamiliaDestino = dto.getSubfamiliaId() != null
                ? resolverSubfamilia(dto.getSubfamiliaId(), familiaDestino)
                : existente.getSubfamilia();
        if (subfamiliaDestino != null && !Objects.equals(subfamiliaDestino.getFamilia().getId(), familiaDestino.getId())) {
            subfamiliaDestino = null;
        }

        boolean codigoDuplicado = existeCodigoEnClasificacion(
                familiaDestino.getId(), subfamiliaDestino, codigoDestino, id);
        if (codigoDuplicado) {
            throw new BadRequestException("Ya existe un modelo con el codigo: " + codigoDestino + " en la clasificación seleccionada");
        }

        boolean nombreDuplicadoActualizacion = existeNombreEnClasificacion(
                familiaDestino.getId(), subfamiliaDestino, nombreDestino, id);
        if (nombreDuplicadoActualizacion) {
            throw new BadRequestException("Ya existe un modelo con el nombre: " + nombreDestino + " en la clasificación seleccionada");
        }

        existente.setCodigo(codigoDestino);
        existente.setNombre(nombreDestino);

        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }

        if (dto.getDescripcionCorta() != null) {
            existente.setDescripcionCorta(dto.getDescripcionCorta());
        }

        if (dto.getUrlImagen() != null) {
            existente.setUrlImagen(dto.getUrlImagen());
        }

        existente.setFamilia(familiaDestino);
        existente.setSubfamilia(subfamiliaDestino);

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        ModeloModel actualizado = modeloRepository.save(existente);
        if (dto.getMateriales() != null) {
            sincronizarMateriales(actualizado, dto.getMateriales());
        }
        if (dto.getCategorias() != null) {
            sincronizarCategorias(actualizado, dto.getCategorias());
        }
        limpiarPlantillaGlobal(actualizado);
        actualizado = modeloRepository.save(actualizado);
        productoPlantillaModeloService.propagarAdiciones(actualizado);
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
    public SincronizacionInsumosVariantesResponseDTO sincronizarInsumosVariantes(
            Long modeloId,
            Long nivelId,
            Long materialId,
            List<ModeloInsumoDTO> insumosDto) {
        ModeloModel modelo = modeloRepository.findById(modeloId)
                .orElseThrow(() -> new NotFoundException("Modelo no encontrado con ID: " + modeloId));
        NivelModel nivel = nivelRepository.findById(nivelId)
                .orElseThrow(() -> new NotFoundException("Categoria del modelo no encontrada con ID: " + nivelId));

        if (nivel.getModelo() == null || !Objects.equals(nivel.getModelo().getId(), modelo.getId())) {
            throw new BadRequestException("La categoria seleccionada no pertenece al modelo");
        }
        validarMaterialPlantilla(modelo, nivel, materialId);

        Map<PlantillaInsumoKey, ModeloInsumoDTO> plantilla = normalizarPlantillaInsumos(modelo, nivel, insumosDto);
        sincronizarInsumosCategoria(nivel, List.copyOf(plantilla.values()));

        int productosActualizados = 0;
        int insumosAgregados = 0;
        int insumosActualizados = 0;
        int insumosEliminados = 0;

        List<ProductoModel> productos = productoRepository.findByModeloIdAndNivelId(modeloId, nivelId).stream()
                .filter(producto -> materialId == null || Objects.equals(getProductoMaterialId(producto), materialId))
                .toList();

        for (ProductoModel producto : productos) {
            List<ProductoInsumoModel> actuales = productoInsumoRepository.findByProductoId(producto.getId());
            Map<Long, ProductoInsumoModel> porInsumo = actuales.stream()
                    .filter(item -> item.getInsumo() != null && item.getInsumo().getId() != null)
                    .collect(Collectors.toMap(
                            item -> item.getInsumo().getId(),
                            item -> item,
                            (primero, segundo) -> primero,
                            LinkedHashMap::new));

            List<ProductoInsumoModel> guardar = new java.util.ArrayList<>();
            List<ProductoInsumoModel> eliminar = new java.util.ArrayList<>();
            Map<Long, ModeloInsumoDTO> plantillaProducto = construirPlantillaParaProducto(plantilla, producto);

            for (ProductoInsumoModel actual : actuales) {
                Long insumoId = actual.getInsumo() != null ? actual.getInsumo().getId() : null;
                if (esInsumoHeredado(actual) && (insumoId == null || !plantillaProducto.containsKey(insumoId))) {
                    eliminar.add(actual);
                }
            }

            for (ModeloInsumoDTO itemPlantilla : plantillaProducto.values()) {
                Long insumoId = itemPlantilla.getId();
                ProductoInsumoModel actual = porInsumo.get(insumoId);
                if (actual == null) {
                    InsumoModel insumo = insumoRepository.findById(insumoId)
                            .orElseThrow(() -> new NotFoundException("Insumo no encontrado con ID: " + insumoId));
                    guardar.add(ProductoInsumoModel.builder()
                            .producto(producto)
                            .insumo(insumo)
                            .cantidad(itemPlantilla.getCantidad())
                            .desperdicioPorcentaje(itemPlantilla.getDesperdicioPorcentaje())
                            .observaciones(OBSERVACION_HEREDADO_CATEGORIA)
                            .build());
                    continue;
                }

                if (esInsumoHeredado(actual)) {
                    boolean cambioCantidad = !Objects.equals(actual.getCantidad(), itemPlantilla.getCantidad());
                    boolean cambioDesperdicio = !Objects.equals(
                            valorSeguro(actual.getDesperdicioPorcentaje()),
                            valorSeguro(itemPlantilla.getDesperdicioPorcentaje()));
                    if (cambioCantidad || cambioDesperdicio) {
                        actual.setCantidad(itemPlantilla.getCantidad());
                        actual.setDesperdicioPorcentaje(itemPlantilla.getDesperdicioPorcentaje());
                        guardar.add(actual);
                    }
                }
            }

            if (!guardar.isEmpty() || !eliminar.isEmpty()) {
                int agregadosProducto = (int) guardar.stream().filter(item -> item.getId() == null).count();
                productosActualizados++;
                insumosAgregados += agregadosProducto;
                insumosActualizados += guardar.size() - agregadosProducto;
                insumosEliminados += eliminar.size();
            }

            if (!eliminar.isEmpty()) {
                productoInsumoRepository.deleteAll(eliminar);
            }
            if (!guardar.isEmpty()) {
                productoInsumoRepository.saveAll(guardar);
            }
        }

        return SincronizacionInsumosVariantesResponseDTO.builder()
                .modeloId(modelo.getId())
                .nivelId(nivel.getId())
                .nivelNombre(nivel.getNombre())
                .productosActualizados(productosActualizados)
                .insumosAgregados(insumosAgregados)
                .insumosActualizados(insumosActualizados)
                .insumosEliminados(insumosEliminados)
                .build();
    }

    private Long getProductoMaterialId(ProductoModel producto) {
        return producto != null && producto.getMaterial() != null ? producto.getMaterial().getId() : null;
    }

    @Transactional
    public void eliminar(Long id) {
        if (!modeloRepository.existsById(id)) {
            throw new NotFoundException("Modelo no encontrado con ID: " + id);
        }

        if (productoRepository.existsByModeloId(id)) {
            throw new BadRequestException("No se puede eliminar el modelo porque tiene productos asociados");
        }

        nivelRepository.findByModeloIdOrderByCodigoAsc(id).forEach(nivel -> {
            nivelInsumoRepository.deleteByNivelId(nivel.getId());
            nivelOperacionRepository.deleteByNivelId(nivel.getId());
        });
        nivelRepository.deleteByModeloId(id);
        modeloRepository.deleteById(id);
    }

    private ModeloCategoriaDTO mapCategoria(NivelModel nivel) {
        ModeloCategoriaDTO dto = new ModeloCategoriaDTO();
        dto.setId(nivel.getId());
        if (nivel.getCategoria() != null) {
            dto.setCategoriaId(nivel.getCategoria().getId());
        }
        dto.setCodigo(nivel.getCodigo());
        dto.setNombre(nivel.getNombre());
        dto.setDescripcion(nivel.getDescripcion());
        dto.setActivo(nivel.getActivo());
        dto.setInsumos(nivelInsumoRepository.findByNivelIdOrderByMaterialNombreAscInsumoNombreAsc(nivel.getId()).stream()
                .map(this::mapNivelInsumo)
                .toList());
        dto.setOperaciones(nivelOperacionRepository.findByNivelIdOrderByOrdenAsc(nivel.getId()).stream()
                .map(this::mapNivelOperacion)
                .toList());
        return dto;
    }

    private ModeloInsumoDTO mapNivelInsumo(NivelInsumoModel nivelInsumo) {
        InsumoModel insumo = nivelInsumo.getInsumo();
        return ModeloInsumoDTO.builder()
                .id(insumo.getId())
                .codigo(insumo.getCodigo())
                .nombre(insumo.getNombre())
                .unidadMedida(insumo.getUnidadMedida() != null ? insumo.getUnidadMedida().getSimbolo() : null)
                .materialId(nivelInsumo.getMaterial() != null ? nivelInsumo.getMaterial().getId() : null)
                .materialCodigo(nivelInsumo.getMaterial() != null ? nivelInsumo.getMaterial().getCodigo() : null)
                .materialNombre(nivelInsumo.getMaterial() != null ? nivelInsumo.getMaterial().getNombre() : null)
                .cantidad(nivelInsumo.getCantidad())
                .desperdicioPorcentaje(valorSeguro(nivelInsumo.getDesperdicioPorcentaje()))
                .costoCotizacion(valorSeguro(insumo.getCostoCotizacion()))
                .activo(insumo.getActivo())
                .build();
    }

    private ModeloOperacionDTO mapNivelOperacion(NivelOperacionModel nivelOperacion) {
        OperacionModel operacion = nivelOperacion.getOperacion();
        return ModeloOperacionDTO.builder()
                .id(operacion.getId())
                .codigo(operacion.getCodigo())
                .nombre(operacion.getNombre())
                .centroTrabajoNombre(operacion.getCentroTrabajo() != null
                        ? operacion.getCentroTrabajo().getNombre()
                        : null)
                .cantidad(nivelOperacion.getCantidad())
                .orden(nivelOperacion.getOrden())
                .activo(operacion.getActivo())
                .build();
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

    private boolean existeCodigoEnClasificacion(
            Long familiaId, SubfamiliaModel subfamilia, String codigo, Long modeloId) {
        if (subfamilia != null) {
            return modeloId == null
                    ? modeloRepository.existsBySubfamiliaIdAndCodigoIgnoreCase(subfamilia.getId(), codigo)
                    : modeloRepository.existsBySubfamiliaIdAndCodigoIgnoreCaseAndIdNot(
                            subfamilia.getId(), codigo, modeloId);
        }
        return modeloId == null
                ? modeloRepository.existsByFamiliaIdAndSubfamiliaIsNullAndCodigoIgnoreCase(familiaId, codigo)
                : modeloRepository.existsByFamiliaIdAndSubfamiliaIsNullAndCodigoIgnoreCaseAndIdNot(
                        familiaId, codigo, modeloId);
    }

    private boolean existeNombreEnClasificacion(
            Long familiaId, SubfamiliaModel subfamilia, String nombre, Long modeloId) {
        if (subfamilia != null) {
            return modeloId == null
                    ? modeloRepository.existsBySubfamiliaIdAndNombreIgnoreCase(subfamilia.getId(), nombre)
                    : modeloRepository.existsBySubfamiliaIdAndNombreIgnoreCaseAndIdNot(
                            subfamilia.getId(), nombre, modeloId);
        }
        return modeloId == null
                ? modeloRepository.existsByFamiliaIdAndSubfamiliaIsNullAndNombreIgnoreCase(familiaId, nombre)
                : modeloRepository.existsByFamiliaIdAndSubfamiliaIsNullAndNombreIgnoreCaseAndIdNot(
                        familiaId, nombre, modeloId);
    }

    private SubfamiliaModel resolverSubfamilia(Long subfamiliaId, FamiliaModel familia) {
        if (subfamiliaId == null) {
            return null;
        }
        SubfamiliaModel subfamilia = subfamiliaRepository.findById(subfamiliaId)
                .orElseThrow(() -> new NotFoundException("Subfamilia no encontrada con ID: " + subfamiliaId));
        if (subfamilia.getFamilia() == null || !Objects.equals(subfamilia.getFamilia().getId(), familia.getId())) {
            throw new BadRequestException("La subfamilia seleccionada no pertenece a la familia del modelo");
        }
        return subfamilia;
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

    private void limpiarPlantillaGlobal(ModeloModel modelo) {
        modelo.getInsumos().clear();
        modelo.getOperaciones().clear();
    }

    private Map<PlantillaInsumoKey, ModeloInsumoDTO> normalizarPlantillaInsumos(
            ModeloModel modelo,
            NivelModel nivel,
            List<ModeloInsumoDTO> insumosDto) {
        Map<PlantillaInsumoKey, ModeloInsumoDTO> plantilla = new LinkedHashMap<>();
        if (insumosDto == null) {
            return plantilla;
        }

        for (ModeloInsumoDTO item : insumosDto) {
            if (item == null || item.getId() == null) {
                continue;
            }
            validarMaterialPlantilla(modelo, nivel, item.getMaterialId());
            PlantillaInsumoKey key = new PlantillaInsumoKey(item.getMaterialId(), item.getId());
            if (plantilla.containsKey(key)) {
                throw new BadRequestException("No se pueden repetir insumos en la misma seccion de la categoria " + nivel.getNombre());
            }
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new BadRequestException("La cantidad del insumo debe ser mayor a cero en la categoria " + nivel.getNombre());
            }
            item.setDesperdicioPorcentaje(validarDesperdicioInsumo(nivel, item.getDesperdicioPorcentaje()));
            plantilla.put(key, item);
        }
        return plantilla;
    }

    private void validarMaterialPlantilla(ModeloModel modelo, NivelModel nivel, Long materialId) {
        if (materialId == null) {
            return;
        }
        boolean asociado = modelo.getMateriales().stream()
                .anyMatch(material -> Objects.equals(material.getId(), materialId));
        if (!asociado) {
            throw new BadRequestException("El material seleccionado en la categoria " + nivel.getNombre() + " no pertenece al modelo");
        }
    }

    private Map<Long, ModeloInsumoDTO> construirPlantillaParaProducto(
            Map<PlantillaInsumoKey, ModeloInsumoDTO> plantilla,
            ProductoModel producto) {
        Map<Long, ModeloInsumoDTO> efectiva = new LinkedHashMap<>();
        Long materialProductoId = producto.getMaterial() != null ? producto.getMaterial().getId() : null;

        plantilla.forEach((key, item) -> {
            if (Objects.equals(key.materialId(), materialProductoId)) {
                efectiva.put(key.insumoId(), item);
            }
        });

        return efectiva;
    }

    private boolean esInsumoHeredado(ProductoInsumoModel item) {
        return item != null && OBSERVACION_HEREDADO_CATEGORIA.equals(item.getObservaciones());
    }

    private void sincronizarCategorias(ModeloModel modelo, List<ModeloCategoriaDTO> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            throw new BadRequestException("El modelo debe tener al menos una categoria");
        }

        List<NivelModel> existentes = nivelRepository.findByModeloIdOrderByCodigoAsc(modelo.getId());
        Set<Long> idsConservados = new HashSet<>();
        Set<Long> categoriasConservadas = new HashSet<>();
        Set<String> codigosUsados = existentes.stream()
                .map(NivelModel::getCodigo)
                .map(codigo -> codigo == null ? "" : codigo.trim())
                .filter(codigo -> !codigo.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (ModeloCategoriaDTO categoria : categorias) {
            NivelModel nivel = null;
            if (categoria.getId() != null) {
                nivel = existentes.stream()
                        .filter(item -> item.getId().equals(categoria.getId()))
                        .findFirst()
                        .orElse(null);
            }

            CategoriaModel catalogo = resolverCategoriaGlobal(categoria, nivel);
            if (catalogo == null) {
                throw new BadRequestException("No se pudo resolver la categoria seleccionada");
            }
            if (!categoriasConservadas.add(catalogo.getId())) {
                throw new BadRequestException("No se pueden repetir categorias dentro del mismo modelo: " + catalogo.getNombre());
            }

            if (nivel == null) {
                nivel = new NivelModel();
                nivel.setModelo(modelo);
                nivel.setCodigo(generarSiguienteCodigo(codigosUsados));
                codigosUsados.add(nivel.getCodigo());
            } else {
                idsConservados.add(nivel.getId());
            }

            nivel.setCategoria(catalogo);
            nivel.setNombre(catalogo.getNombre());
            nivel.setDescripcion(catalogo.getDescripcion());
            nivel.setActivo(catalogo.getActivo() == null || Boolean.TRUE.equals(catalogo.getActivo()));

            NivelModel guardado = nivelRepository.save(nivel);
            sincronizarInsumosCategoria(guardado, categoria.getInsumos());
            sincronizarOperacionesCategoria(guardado, categoria.getOperaciones());
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
            nivelInsumoRepository.deleteByNivelId(existente.getId());
            nivelOperacionRepository.deleteByNivelId(existente.getId());
            nivelRepository.delete(existente);
        }
    }

    private void sincronizarInsumosCategoria(NivelModel nivel, List<ModeloInsumoDTO> insumosDto) {
        nivelInsumoRepository.deleteByNivelId(nivel.getId());
        if (insumosDto == null || insumosDto.isEmpty()) {
            return;
        }

        Set<PlantillaInsumoKey> idsUnicos = new HashSet<>();
        List<NivelInsumoModel> insumos = new java.util.ArrayList<>();
        for (ModeloInsumoDTO item : insumosDto) {
            if (item == null || item.getId() == null) {
                continue;
            }
            validarMaterialPlantilla(nivel.getModelo(), nivel, item.getMaterialId());
            PlantillaInsumoKey key = new PlantillaInsumoKey(item.getMaterialId(), item.getId());
            if (!idsUnicos.add(key)) {
                throw new BadRequestException("No se pueden repetir insumos en la misma seccion de la categoria " + nivel.getNombre());
            }
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new BadRequestException("La cantidad del insumo debe ser mayor a cero en la categoria " + nivel.getNombre());
            }
            double desperdicio = validarDesperdicioInsumo(nivel, item.getDesperdicioPorcentaje());
            InsumoModel insumo = insumoRepository.findById(item.getId())
                    .orElseThrow(() -> new NotFoundException("Insumo no encontrado con ID: " + item.getId()));
            insumos.add(NivelInsumoModel.builder()
                    .nivel(nivel)
                    .insumo(insumo)
                    .material(item.getMaterialId() != null
                            ? materialRepository.findById(item.getMaterialId())
                                    .orElseThrow(() -> new NotFoundException("Material no encontrado con ID: " + item.getMaterialId()))
                            : null)
                    .cantidad(item.getCantidad())
                    .desperdicioPorcentaje(desperdicio)
                    .build());
        }
        nivelInsumoRepository.saveAll(insumos);
    }

    private double validarDesperdicioInsumo(NivelModel nivel, Double desperdicioPorcentaje) {
        double desperdicio = desperdicioPorcentaje != null ? desperdicioPorcentaje : 0.0;
        if (desperdicio < 0) {
            throw new BadRequestException("El desperdicio del insumo debe ser mayor o igual a cero en la categoria " + nivel.getNombre());
        }
        return desperdicio;
    }

    private double valorSeguro(Double valor) {
        return valor != null ? valor : 0.0;
    }

    private void sincronizarOperacionesCategoria(NivelModel nivel, List<ModeloOperacionDTO> operacionesDto) {
        nivelOperacionRepository.deleteByNivelId(nivel.getId());
        if (operacionesDto == null || operacionesDto.isEmpty()) {
            return;
        }

        Set<Long> idsUnicos = new HashSet<>();
        List<NivelOperacionModel> operaciones = new java.util.ArrayList<>();
        int orden = 1;
        for (ModeloOperacionDTO item : operacionesDto) {
            if (item == null || item.getId() == null) {
                continue;
            }
            if (!idsUnicos.add(item.getId())) {
                throw new BadRequestException("No se pueden repetir operaciones en la categoria " + nivel.getNombre());
            }
            Integer cantidad = item.getCantidad() != null ? item.getCantidad() : 1;
            if (cantidad < 1) {
                throw new BadRequestException("La cantidad de la operacion debe ser al menos 1 en la categoria " + nivel.getNombre());
            }
            OperacionModel operacion = operacionRepository.findById(item.getId())
                    .orElseThrow(() -> new NotFoundException("Operacion no encontrada con ID: " + item.getId()));
            operaciones.add(NivelOperacionModel.builder()
                    .nivel(nivel)
                    .operacion(operacion)
                    .cantidad(cantidad)
                    .orden(orden++)
                    .build());
        }
        nivelOperacionRepository.saveAll(operaciones);
    }

    private CategoriaModel resolverCategoriaGlobal(ModeloCategoriaDTO categoria, NivelModel nivelExistente) {
        if (categoria.getCategoriaId() != null) {
            return categoriaRepository.findById(categoria.getCategoriaId())
                    .orElseThrow(() -> new BadRequestException("No existe la categoria seleccionada"));
        }

        if (nivelExistente != null && nivelExistente.getCategoria() != null) {
            return nivelExistente.getCategoria();
        }

        String nombre = categoria.getNombre() == null ? "" : categoria.getNombre().trim();
        if (nombre.isBlank()) {
            return null;
        }

        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> {
                    CategoriaModel nueva = new CategoriaModel();
                    nueva.setNombre(nombre);
                    nueva.setDescripcion(categoria.getDescripcion());
                    nueva.setActivo(categoria.getActivo() == null || Boolean.TRUE.equals(categoria.getActivo()));
                    return categoriaRepository.save(nueva);
                });
    }

    private String generarSiguienteCodigo(Set<String> codigosUsados) {
        int maximo = codigosUsados.stream()
                .mapToInt(this::parseCodigoSecuencial)
                .max()
                .orElse(0);
        String siguiente = String.format(Locale.ROOT, "%02d", maximo + 1);
        while (codigosUsados.contains(siguiente)) {
            maximo += 1;
            siguiente = String.format(Locale.ROOT, "%02d", maximo + 1);
        }
        return siguiente;
    }

    private int parseCodigoSecuencial(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(codigo.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
