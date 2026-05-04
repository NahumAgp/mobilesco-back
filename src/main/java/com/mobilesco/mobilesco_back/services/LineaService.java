// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/LineaService.java
// ============================================
package com.mobilesco.mobilesco_back.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Locale;
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

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaCreateDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaResponseDTO;
import com.mobilesco.mobilesco_back.dto.linea.LineaUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.LineaModel;
import com.mobilesco.mobilesco_back.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.repositories.LineaRepository;

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

    public LineaResponseDTO crear(LineaCreateDTO dto) {

        if (lineaRepository.existsByCodigo(dto.getCodigo())) {
            throw new BadRequestException("Ya existe una linea con el codigo: " + dto.getCodigo());
        }

        if (lineaRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe una linea con el nombre: " + dto.getNombre());
        }

        LineaModel linea = new LineaModel();
        linea.setCodigo(dto.getCodigo());
        linea.setNombre(dto.getNombre());
        linea.setDescripcion(dto.getDescripcion());
        linea.setOrden(Objects.requireNonNullElse(dto.getOrden(), 0));
        linea.setActivo(true);

        LineaModel guardado = lineaRepository.save(linea);
        return mapToResponseDTO(guardado);
    }

    // ========== READ ==========

    private Sort construirSortLineas(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "orden").and(Sort.by(Sort.Direction.ASC, "id"));
        }

        String campoNormalizado = sortBy.trim().toLowerCase(Locale.ROOT);

        switch (campoNormalizado) {
            case "id":
                return Sort.by(sortDirection, "id");
            case "codigo":
                return Sort.by(sortDirection, "codigo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "nombre":
                return Sort.by(sortDirection, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
            case "descripcion":
                return Sort.by(sortDirection, "descripcion").and(Sort.by(Sort.Direction.ASC, "id"));
            case "orden":
                return Sort.by(sortDirection, "orden").and(Sort.by(Sort.Direction.ASC, "id"));
            case "activo":
                return Sort.by(sortDirection, "activo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "createdat":
            case "created_at":
                return Sort.by(sortDirection, "createdAt").and(Sort.by(Sort.Direction.ASC, "id"));
            default:
                return Sort.by(Sort.Direction.ASC, "orden").and(Sort.by(Sort.Direction.ASC, "id"));
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

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Lineas");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "ID", "Código", "Nombre", "Descripción", "Orden", "Estado", "Creada"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (LineaResponseDTO linea : filtradas) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(linea.getId() != null ? linea.getId() : 0L);
                row.createCell(1).setCellValue(nvl(linea.getCodigo()));
                row.createCell(2).setCellValue(nvl(linea.getNombre()));
                row.createCell(3).setCellValue(nvl(linea.getDescripcion()));
                row.createCell(4).setCellValue(linea.getOrden() != null ? linea.getOrden() : 0);
                row.createCell(5).setCellValue(Boolean.TRUE.equals(linea.getActivo()) ? "Activo" : "Inactivo");
                row.createCell(6).setCellValue(linea.getCreatedAt() != null ? linea.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el reporte de lineas", e);
        }
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
            if (lineaRepository.existsByCodigo(dto.getCodigo())) {
                throw new BadRequestException("Ya existe una linea con el codigo: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo());
        }

        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (lineaRepository.existsByNombre(dto.getNombre())) {
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
