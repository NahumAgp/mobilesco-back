package com.mobilesco.mobilesco_back.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.dto.Insumo.InsumoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Insumo.InsumoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Insumo.InsumoUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.InsumoModel;
import com.mobilesco.mobilesco_back.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.repositories.UnidadMedidaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsumoService {

    private static final int PAGE_SIZE = 10;

    private final InsumoRepository insumoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;

    /**
     * ACTUALIZAR un insumo existente
     */
    @Transactional
    public InsumoResponseDTO actualizar(Long id, InsumoUpdateDTO dto) {
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

        // Actualizar campos
        insumo.setCodigo(dto.getCodigo());
        insumo.setNombre(dto.getNombre());
        insumo.setDescripcion(dto.getDescripcion());
        insumo.setUbicacion(dto.getUbicacion());
        insumo.setFila(dto.getFila());
        insumo.setColumna(dto.getColumna());
        
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

    // Crear entidad
    InsumoModel insumo = InsumoModel.builder()
            .codigo(dto.getCodigo())
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .ubicacion(dto.getUbicacion())
            .fila(dto.getFila())
            .columna(dto.getColumna())
            .unidadMedida(unidadMedida)
            .stockMinimo(stockMinimo)  // ✅ Ya es Double, no hay unboxing
            .stockActual(0.0)           // ✅ Double literal
            .activo(true)                // ✅ Boolean literal
            .build();

    InsumoModel saved = insumoRepository.save(insumo);
    saved.setCodigoBarras(generarCodigoBarras(saved.getId()));
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

    private Sort construirSortInsumos(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
        }

        String campoNormalizado = sortBy.trim().toLowerCase(Locale.ROOT);

        switch (campoNormalizado) {
            case "id":
                return Sort.by(sortDirection, "id");
            case "codigo":
                return Sort.by(sortDirection, "codigo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "nombre":
                return Sort.by(sortDirection, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
            case "ubicacion":
                return Sort.by(sortDirection, "ubicacion").and(Sort.by(Sort.Direction.ASC, "id"));
            case "stockactual":
            case "stock_actual":
                return Sort.by(sortDirection, "stockActual").and(Sort.by(Sort.Direction.ASC, "id"));
            case "stockminimo":
            case "stock_minimo":
                return Sort.by(sortDirection, "stockMinimo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "activo":
                return Sort.by(sortDirection, "activo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "fecharegistro":
            case "fecha_registro":
                return Sort.by(sortDirection, "fechaRegistro").and(Sort.by(Sort.Direction.ASC, "id"));
            default:
                return Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
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

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Insumos");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "ID", "Codigo", "Codigo barras", "Nombre", "Descripcion", "Ubicacion", "Fila", "Columna",
                    "Unidad", "Stock actual", "Stock minimo", "Estado", "Fecha registro"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (InsumoResponseDTO insumo : filtrados) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(insumo.getId() != null ? insumo.getId() : 0L);
                row.createCell(1).setCellValue(nvl(insumo.getCodigo()));
                row.createCell(2).setCellValue(nvl(insumo.getCodigoBarras()));
                row.createCell(3).setCellValue(nvl(insumo.getNombre()));
                row.createCell(4).setCellValue(nvl(insumo.getDescripcion()));
                row.createCell(5).setCellValue(nvl(insumo.getUbicacion()));
                row.createCell(6).setCellValue(nvl(insumo.getFila()));
                row.createCell(7).setCellValue(nvl(insumo.getColumna()));
                row.createCell(8).setCellValue(nvl(insumo.getUnidadMedidaSimbolo()));
                row.createCell(9).setCellValue(insumo.getStockActual() != null ? insumo.getStockActual() : 0.0);
                row.createCell(10).setCellValue(insumo.getStockMinimo() != null ? insumo.getStockMinimo() : 0.0);
                row.createCell(11).setCellValue(Boolean.TRUE.equals(insumo.getActivo()) ? "Activo" : "Inactivo");
                row.createCell(12).setCellValue(insumo.getFechaRegistro() != null ? insumo.getFechaRegistro().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el reporte de insumos", e);
        }
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
                        insumo.getUbicacion(),
                        insumo.getFila(),
                        insumo.getColumna(),
                        insumo.getUnidadMedidaNombre(),
                        insumo.getUnidadMedidaSimbolo(),
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

    private String generarCodigoBarras(Long id) {
        long valor = id == null ? 0L : Math.floorMod(id, 1_000_000_000L);
        String base = "750" + String.format("%09d", valor);
        return base + calcularDigitoVerificadorEan13(base);
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
     * ELIMINAR (desactivar) insumo
     */
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando (desactivando) insumo ID: {}", id);
        
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));
        
        insumo.setActivo(false);
        insumoRepository.save(insumo);
        
        log.info("Insumo desactivado correctamente");
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
                .ubicacion(insumo.getUbicacion())
                .fila(insumo.getFila())
                .columna(insumo.getColumna())   
                .unidadMedidaId(insumo.getUnidadMedida().getId())
                .unidadMedidaNombre(insumo.getUnidadMedida().getNombre())
                .unidadMedidaSimbolo(insumo.getUnidadMedida().getSimbolo())
                .stockActual(insumo.getStockActual())
                .stockMinimo(insumo.getStockMinimo())
                .activo(insumo.getActivo())
                .fechaRegistro(insumo.getFechaRegistro())
                .fechaActualizacion(insumo.getFechaActualizacion())
                .build();
    }
}
