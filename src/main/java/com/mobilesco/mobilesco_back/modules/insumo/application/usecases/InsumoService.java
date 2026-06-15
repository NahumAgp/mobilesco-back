package com.mobilesco.mobilesco_back.modules.insumo.application.usecases;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.DetalleCompraModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.insumo.domain.enums.TipoInsumo;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoCostoResponseDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.out.persistence.repositories.KardexRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories.DetalleSalidaInsumoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories.UnidadMedidaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsumoService {

    private static final int PAGE_SIZE = 10;
    private static final List<String> ROLES_COSTOS = List.of(
            "ROLE_ADMIN",
            "ROLE_SUPER_ADMIN",
            "ROLE_SUBDIRECCION_ADMINISTRATIVA"
    );
    private static final List<String> ROLES_GESTION_INSUMOS = List.of(
            "ROLE_ADMIN",
            "ROLE_SUPER_ADMIN",
            "ROLE_DIRECTOR_GENERAL",
            "ROLE_JEFE_ALMACEN",
            "ROLE_ALMACEN",
            "ROLE_SUBDIRECCION_ADMINISTRATIVA"
    );

    private final InsumoRepository insumoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final KardexService kardexService;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final KardexRepository kardexRepository;
    private final DetalleSalidaInsumoRepository detalleSalidaInsumoRepository;

    /**
     * ACTUALIZAR un insumo existente
     */
    @Transactional
    public InsumoResponseDTO actualizar(Long id, InsumoUpdateDTO dto) {
        validarPermisoGestionInsumos();
        log.info("Actualizando insumo ID: {}", id);
        
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));

        // Validar nombre único (excepto si es el mismo)
        if (!insumo.getNombre().equalsIgnoreCase(dto.getNombre()) && 
                insumoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe un insumo con el nombre: " + dto.getNombre());
        }

        // Validar unidad de medida si cambió
        if (dto.getUnidadMedidaId() != null) {
            
            // 🔴 CORRECCIÓN 1: Obtener el ID como Long y comparar correctamente
            Long unidadActualId = insumo.getUnidadMedida().getId();
            
            // 🔴 CORRECCIÓN 2: Comparación correcta entre Long objetos
            if (!unidadActualId.equals(dto.getUnidadMedidaId())) {
                
                UnidadMedidaModel nuevaUnidad = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                            "Unidad de medida no encontrada con id: " + dto.getUnidadMedidaId()));
                
                if (!nuevaUnidad.getEstado()) {
                    throw new ValidationException("La unidad de medida está inactiva: " + nuevaUnidad.getNombre());
                }
                
                insumo.setUnidadMedida(nuevaUnidad);
            }
        }

        // El codigo queda administrado por el backend; si viene uno nuevo, se respeta.
        if (dto.getCodigo() != null && !dto.getCodigo().isBlank()) {
            insumo.setCodigo(dto.getCodigo().trim());
        }
        insumo.setNombre(dto.getNombre());
        insumo.setDescripcion(dto.getDescripcion());
        insumo.setTipoInsumo(dto.getTipoInsumo());
        insumo.setUbicacion(dto.getUbicacion());
        insumo.setFila(dto.getFila());
        insumo.setColumna(dto.getColumna());
        if (dto.getCostoCotizacion() != null && !Objects.equals(insumo.getCostoCotizacion(), dto.getCostoCotizacion())) {
            validarPermisoGestionCostos();
            insumo.setCostoCotizacion(dto.getCostoCotizacion());
        }
        
        if (dto.getStockMinimo() != null) {
            insumo.setStockMinimo(dto.getStockMinimo());
        }
        
        if (dto.getStockActual() != null) {
            insumo.setStockActual(dto.getStockActual());
        }
        
        if (dto.getActivo() != null) {
            insumo.setActivo(dto.getActivo());
        }

        InsumoModel updated = insumoRepository.save(insumo);
        log.info("Insumo actualizado: {}", updated.getNombre());
        
        return mapToResponseDTO(updated);
    }

    /**
     * CREAR un nuevo insumo
     */
  /**
 * CREAR un nuevo insumo
 */
@Transactional
public InsumoResponseDTO crear(InsumoCreateDTO dto) {
    validarPermisoGestionInsumos();
    log.info("Creando nuevo insumo: {}", dto.getNombre());
    
    // Validar nombre único
    if (insumoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
        throw new ValidationException("Ya existe un insumo con el nombre: " + dto.getNombre());
    }

    // Validar que la unidad de medida exista y esté activa
    UnidadMedidaModel unidadMedida = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
            .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con id: " + dto.getUnidadMedidaId()));
    
    if (!unidadMedida.getEstado()) {
        throw new ValidationException("La unidad de medida está inactiva: " + unidadMedida.getNombre());
    }

    // 🔴 CORREGIDO: Manejo seguro de valores null
    Double stockMinimo = dto.getStockMinimo();
    if (stockMinimo == null) {
        stockMinimo = 0.0;  // Usar Double, no int
    }

    Double stockActual = dto.getStockActual();
    if (stockActual == null) {
        stockActual = 0.0;
    }

    String codigoInicial = dto.getCodigo();
    if (codigoInicial == null || codigoInicial.isBlank()) {
        codigoInicial = generarCodigoTemporal();
    }

    if (dto.getCostoCotizacion() != null) {
        validarPermisoGestionCostos();
    }

    // Crear entidad
    InsumoModel insumo = InsumoModel.builder()
            .codigo(codigoInicial.trim())
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .tipoInsumo(dto.getTipoInsumo())
            .ubicacion(dto.getUbicacion())
            .fila(dto.getFila())
            .columna(dto.getColumna())
            .costoCotizacion(dto.getCostoCotizacion())
            .unidadMedida(unidadMedida)
            .stockMinimo(stockMinimo)  // ✅ Ya es Double, no hay unboxing
            .stockActual(stockActual)
            .activo(true)                // ✅ Boolean literal
            .build();

    InsumoModel saved = insumoRepository.save(insumo);
    saved.setCodigoBarras(generarCodigoBarras(saved.getId()));
    saved.setCodigo(saved.getCodigoBarras());
    saved = insumoRepository.save(saved);
    log.info("Insumo creado con ID: {}", saved.getId());
    
    return mapToResponseDTO(saved);
}

    /**
     * OBTENER insumo por ID
     */
    @Transactional(readOnly = true)
    public InsumoResponseDTO obtenerPorId(Long id) {
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));
        return mapToResponseDTO(insumo);
    }

    /**
     * LISTAR todos los insumos
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listar() {
        return insumoRepository.findAll(construirSortInsumos("nombre", "asc"))
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<InsumoResponseDTO> listarPaginado(int page, Integer size, String sortBy, String direction) {
        int pageNumber = Math.max(page, 0);
        int pageSize = size == null || size <= 0 ? PAGE_SIZE : Math.min(size, 100);
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, construirSortInsumos(sortBy, direction));

        Page<InsumoResponseDTO> result = insumoRepository.findAll(pageable).map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<InsumoCostoResponseDTO> listarCostosPaginado(int page, Integer size, String sortBy, String direction) {
        int pageNumber = Math.max(page, 0);
        int pageSize = size == null || size <= 0 ? PAGE_SIZE : Math.min(size, 100);
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, construirSortInsumos(sortBy, direction));

        Page<InsumoCostoResponseDTO> result = insumoRepository.findAll(pageable).map(this::mapToCostoResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public InsumoCostoResponseDTO actualizarCostoCotizacion(Long id, Double costoCotizacion) {
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));

        if (costoCotizacion == null || costoCotizacion <= 0) {
            throw new ValidationException("El costo de cotizacion debe ser mayor a 0");
        }

        validarPermisoGestionCostos();
        insumo.setCostoCotizacion(costoCotizacion);
        return mapToCostoResponseDTO(insumoRepository.save(insumo));
    }

    @Transactional
    public InsumoResponseDTO actualizarEstado(Long id, Boolean activo) {
        validarPermisoGestionInsumos();
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));

        insumo.setActivo(activo);
        return mapToResponseDTO(insumoRepository.save(insumo));
    }

    private Sort construirSortInsumos(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getNombre, InsumoModel::getId);
        }

        String campoNormalizado = sortBy.trim().toLowerCase(Locale.ROOT);

        switch (campoNormalizado) {
            case "id":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descById(InsumoModel.class, InsumoModel::getId)
                        : TypeSafeSorts.ascById(InsumoModel.class, InsumoModel::getId);
            case "codigo":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(InsumoModel.class, InsumoModel::getCodigo, InsumoModel::getId)
                        : TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getCodigo, InsumoModel::getId);
            case "nombre":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(InsumoModel.class, InsumoModel::getNombre, InsumoModel::getId)
                        : TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getNombre, InsumoModel::getId);
            case "ubicacion":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(InsumoModel.class, InsumoModel::getUbicacion, InsumoModel::getId)
                        : TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getUbicacion, InsumoModel::getId);
            case "stockactual":
            case "stock_actual":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(InsumoModel.class, InsumoModel::getStockActual, InsumoModel::getId)
                        : TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getStockActual, InsumoModel::getId);
            case "stockminimo":
            case "stock_minimo":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(InsumoModel.class, InsumoModel::getStockMinimo, InsumoModel::getId)
                        : TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getStockMinimo, InsumoModel::getId);
            case "costocotizacion":
            case "costo_cotizar":
            case "costo_cotizacion":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(InsumoModel.class, InsumoModel::getCostoCotizacion, InsumoModel::getId)
                        : TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getCostoCotizacion, InsumoModel::getId);
            case "activo":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(InsumoModel.class, InsumoModel::getActivo, InsumoModel::getId)
                        : TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getActivo, InsumoModel::getId);
            case "fecharegistro":
            case "fecha_registro":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(InsumoModel.class, InsumoModel::getFechaRegistro, InsumoModel::getId)
                        : TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getFechaRegistro, InsumoModel::getId);
            default:
                return TypeSafeSorts.ascWithId(InsumoModel.class, InsumoModel::getNombre, InsumoModel::getId);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean activo, Boolean stockBajo, String busqueda, String sortBy, String direction) {
        List<InsumoResponseDTO> insumos = insumoRepository.findAll(construirSortInsumos(sortBy, direction))
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        List<InsumoResponseDTO> filtrados = insumos.stream()
                .filter(insumo -> activo == null || Objects.equals(insumo.getActivo(), activo))
                .filter(insumo -> !Boolean.TRUE.equals(stockBajo) || esStockBajo(insumo))
                .filter(insumo -> coincideBusqueda(insumo, busqueda))
                .collect(Collectors.toList());

        String[] headers = {
                "ID", "Codigo", "Codigo barras", "Nombre", "Descripcion", "Ubicacion", "Fila", "Columna",
                "Tipo de insumo", "Unidad", "Stock actual", "Stock minimo", "Costo cotizacion", "Estado", "Fecha registro"
        };

        return ExcelReportBuilder.generate(
                "Insumos",
                "Reporte de insumos",
                headers,
                filtrados.stream()
                        .map(insumo -> new Object[] {
                                insumo.getId() != null ? insumo.getId() : 0L,
                                nvl(insumo.getCodigo()),
                                nvl(insumo.getCodigoBarras()),
                                nvl(insumo.getNombre()),
                                nvl(insumo.getDescripcion()),
                                insumo.getTipoInsumo() != null ? insumo.getTipoInsumo().name() : "",
                                nvl(insumo.getUbicacion()),
                                nvl(insumo.getFila()),
                                nvl(insumo.getColumna()),
                                nvl(insumo.getUnidadMedidaSimbolo()),
                                insumo.getStockActual() != null ? insumo.getStockActual() : 0.0,
                                insumo.getStockMinimo() != null ? insumo.getStockMinimo() : 0.0,
                                insumo.getCostoCotizacion() != null ? insumo.getCostoCotizacion() : 0.0,
                                Boolean.TRUE.equals(insumo.getActivo()) ? "Activo" : "Inactivo",
                                insumo.getFechaRegistro() != null ? insumo.getFechaRegistro().toString() : ""
                        })
                        .collect(Collectors.toList()));
    }

    private boolean coincideBusqueda(InsumoResponseDTO insumo, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        insumo.getId() != null ? String.valueOf(insumo.getId()) : null,
                        insumo.getCodigo(),
                        insumo.getCodigoBarras(),
                        insumo.getNombre(),
                        insumo.getDescripcion(),
                        insumo.getTipoInsumo() != null ? insumo.getTipoInsumo().name() : null,
                        insumo.getUbicacion(),
                        insumo.getFila(),
                        insumo.getColumna(),
                        insumo.getUnidadMedidaNombre(),
                        insumo.getUnidadMedidaSimbolo(),
                        insumo.getCostoCotizacion() != null ? String.valueOf(insumo.getCostoCotizacion()) : null,
                        insumo.getActivo() != null ? (insumo.getActivo() ? "activo" : "inactivo") : null,
                        insumo.getFechaRegistro() != null ? insumo.getFechaRegistro().toString() : null)
                .filter(valor -> valor != null && !valor.isBlank())
                .map(valor -> valor.toLowerCase(Locale.ROOT))
                .anyMatch(valor -> valor.contains(termino));
    }

    private boolean esStockBajo(InsumoResponseDTO insumo) {
        return insumo.getStockMinimo() != null
                && insumo.getStockActual() != null
                && insumo.getStockActual() <= insumo.getStockMinimo();
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private void validarPermisoGestionCostos() {
        if (!puedeGestionarCostos()) {
            throw new ValidationException("No tienes permisos para modificar costos de cotizacion");
        }
    }

    private void validarPermisoGestionInsumos() {
        if (!puedeGestionarInsumos()) {
            throw new ValidationException("No tienes permisos para gestionar insumos");
        }
    }

    private boolean puedeGestionarCostos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLES_COSTOS.contains(authority.getAuthority()));
    }

    private boolean puedeGestionarInsumos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLES_GESTION_INSUMOS.contains(authority.getAuthority()));
    }

    private String generarCodigoBarras(Long id) {
        long valor = id == null ? 0L : Math.floorMod(id, 1_000_000_000L);
        String base = "750" + String.format("%09d", valor);
        return base + calcularDigitoVerificadorEan13(base);
    }

    private String generarCodigoTemporal() {
        return "INS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT);
    }

    private int calcularDigitoVerificadorEan13(String base12) {
        int suma = 0;

        for (int i = 0; i < base12.length(); i++) {
            int digito = Character.digit(base12.charAt(i), 10);
            suma += (i % 2 == 0) ? digito : digito * 3;
        }

        return (10 - (suma % 10)) % 10;
    }

    /**
     * LISTAR solo insumos activos
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listarActivos() {
        return insumoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * BUSCAR insumos por nombre
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> buscar(String nombre) {
        return insumoRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR por unidad de medida
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listarPorUnidadMedida(Long unidadMedidaId) {
        return insumoRepository.findByUnidadMedidaId(unidadMedidaId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR insumos con stock bajo
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listarStockBajo() {
        return insumoRepository.findWithStockBajo()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * AJUSTAR stock manualmente
     */
    @Transactional
    public InsumoResponseDTO ajustarStock(Long id, Double cantidad, String tipo, String motivo) {
        log.info("Ajustando stock - Insumo ID: {}, Cantidad: {}, Tipo: {}", id, cantidad, tipo);
        
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));
        
        if ("ENTRADA".equalsIgnoreCase(tipo)) {
            insumo.setStockActual(insumo.getStockActual() + cantidad);
            log.info("Entrada de stock. Nuevo stock: {}", insumo.getStockActual());
            
        } else if ("SALIDA".equalsIgnoreCase(tipo)) {
            if (insumo.getStockActual() < cantidad) {
                throw new ValidationException(String.format(
                    "Stock insuficiente. Actual: %.2f %s, solicitado: %.2f %s",
                    insumo.getStockActual(), 
                    insumo.getUnidadMedida().getSimbolo(),
                    cantidad, 
                    insumo.getUnidadMedida().getSimbolo()));
            }
            insumo.setStockActual(insumo.getStockActual() - cantidad);
            log.info("Salida de stock. Nuevo stock: {}", insumo.getStockActual());
            
        } else {
            throw new ValidationException("Tipo debe ser 'ENTRADA' o 'SALIDA'");
        }
        
        InsumoModel updated = insumoRepository.save(insumo);
        return mapToResponseDTO(updated);
    }

    /**
     * ELIMINAR definitivamente un insumo sin asociaciones
     */
    @Transactional
    public void eliminar(Long id) {
        validarPermisoGestionInsumos();
        log.info("Eliminando definitivamente insumo ID: {}", id);
        
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));

        if (!puedeEliminarDefinitivo(insumo.getId())) {
            throw new ValidationException("No se puede eliminar el insumo porque tiene compras, movimientos, salidas o productos asociados");
        }

        insumoRepository.delete(insumo);
        log.info("Insumo eliminado definitivamente");
    }

    /**
     * Mapear de Entity a ResponseDTO
     */
    private InsumoResponseDTO mapToResponseDTO(InsumoModel insumo) {
        return InsumoResponseDTO.builder()
                .id(insumo.getId())
                .codigo(insumo.getCodigo())
                .codigoBarras(insumo.getCodigoBarras())
                .nombre(insumo.getNombre())
                .descripcion(insumo.getDescripcion())
                .tipoInsumo(insumo.getTipoInsumo())
                .ubicacion(insumo.getUbicacion())
                .fila(insumo.getFila())
                .columna(insumo.getColumna())   
                .unidadMedidaId(insumo.getUnidadMedida().getId())
                .unidadMedidaNombre(insumo.getUnidadMedida().getNombre())
                .unidadMedidaSimbolo(insumo.getUnidadMedida().getSimbolo())
                .stockActual(insumo.getStockActual())
                .stockMinimo(insumo.getStockMinimo())
                .ultimoCostoCompra(obtenerUltimoCostoCompra(insumo.getId()))
                .costoPromedio(kardexService.calcularCostoPromedio(insumo.getId()))
                .costoCotizacion(insumo.getCostoCotizacion())
                .puedeEliminar(puedeEliminarDefinitivo(insumo.getId()))
                .activo(insumo.getActivo())
                .fechaRegistro(insumo.getFechaRegistro())
                .fechaActualizacion(insumo.getFechaActualizacion())
                .build();
    }

    private InsumoCostoResponseDTO mapToCostoResponseDTO(InsumoModel insumo) {
        return InsumoCostoResponseDTO.builder()
                .id(insumo.getId())
                .codigo(insumo.getCodigo())
                .codigoBarras(insumo.getCodigoBarras())
                .nombre(insumo.getNombre())
                .unidadMedidaId(insumo.getUnidadMedida().getId())
                .unidadMedidaNombre(insumo.getUnidadMedida().getNombre())
                .unidadMedidaSimbolo(insumo.getUnidadMedida().getSimbolo())
                .ultimoCostoCompra(obtenerUltimoCostoCompra(insumo.getId()))
                .costoPromedio(kardexService.calcularCostoPromedio(insumo.getId()))
                .costoCotizacion(insumo.getCostoCotizacion())
                .activo(insumo.getActivo())
                .fechaActualizacion(insumo.getFechaActualizacion())
                .build();
    }

    private Double obtenerUltimoCostoCompra(Long insumoId) {
        List<DetalleCompraModel> ultimasCompras = detalleCompraRepository
                .findUltimasComprasRecibidasByInsumo(insumoId, PageRequest.of(0, 1));

        if (ultimasCompras.isEmpty()) {
            return 0.0;
        }

        Double costo = ultimasCompras.get(0).getCostoPorUnidadConsumo();
        return costo != null ? costo : 0.0;
    }

    private boolean puedeEliminarDefinitivo(Long insumoId) {
        return !detalleCompraRepository.existsByInsumoId(insumoId)
                && !productoInsumoRepository.existsByInsumoId(insumoId)
                && !kardexRepository.existsByInsumoId(insumoId)
                && !detalleSalidaInsumoRepository.existsByInsumoId(insumoId);
    }

    @Transactional(readOnly = true)
    public TipoInsumo[] getTodosLosTipos() {
        return TipoInsumo.values();
    }
}
