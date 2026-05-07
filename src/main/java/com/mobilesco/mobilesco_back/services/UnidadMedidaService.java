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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.dto.unidadMedida.UnidadMedidaCreateDTO;
import com.mobilesco.mobilesco_back.dto.unidadMedida.UnidadMedidaResponseDTO;
import com.mobilesco.mobilesco_back.dto.unidadMedida.UnidadMedidaUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.repositories.UnidadMedidaRepository;


@Service
public class UnidadMedidaService {

    private static final int PAGE_SIZE = 10;

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    private UnidadMedidaResponseDTO mapToResponseDTO(UnidadMedidaModel unidadMedida) {
        UnidadMedidaResponseDTO dto = new UnidadMedidaResponseDTO();
        dto.setId(unidadMedida.getId());
        dto.setNombre(unidadMedida.getNombre());
        dto.setSimbolo(unidadMedida.getSimbolo());
        dto.setTipo(unidadMedida.getTipo());
        dto.setEstado(unidadMedida.getEstado());
        dto.setFechaRegistro(unidadMedida.getFechaRegistro());
        return dto;
    }

    private List<UnidadMedidaResponseDTO> mapToResponseDTOList(List<UnidadMedidaModel> unidades) {
        return unidades.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --------- CREATE ---------
    public UnidadMedidaResponseDTO crear(UnidadMedidaCreateDTO umM) {
        UnidadMedidaModel unidadMedida = new UnidadMedidaModel();
        unidadMedida.setNombre(umM.getNombre());
        unidadMedida.setSimbolo(umM.getSimbolo());
        unidadMedida.setTipo(umM.getTipo());
        unidadMedida.setEstado(true); //<-- regla: nueva unidad inicia activa
        UnidadMedidaModel guardado = unidadMedidaRepository.save(unidadMedida);
        return mapToResponseDTO(guardado);
    }

    // --------- READ ---------
    public List<UnidadMedidaResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(unidadMedidaRepository.findAll(construirSortUnidades("nombre", "asc")));
    }

    public PageResponseDTO<UnidadMedidaResponseDTO> obtenerPaginado(int page, Integer size, String sortBy, String direction) {
        int pageNumber = Math.max(page, 0);
        int pageSize = size == null || size <= 0 ? PAGE_SIZE : Math.min(size, 100);
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, construirSortUnidades(sortBy, direction));

        Page<UnidadMedidaResponseDTO> result = unidadMedidaRepository.findAll(pageable).map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private Sort construirSortUnidades(String sortBy, String direction) {
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
            case "nombre":
                return Sort.by(sortDirection, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
            case "simbolo":
                return Sort.by(sortDirection, "simbolo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "tipo":
                return Sort.by(sortDirection, "tipo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "estado":
                return Sort.by(sortDirection, "estado").and(Sort.by(Sort.Direction.ASC, "id"));
            case "fecharegistro":
            case "fecha_registro":
                return Sort.by(sortDirection, "fechaRegistro").and(Sort.by(Sort.Direction.ASC, "id"));
            default:
                return Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "id"));
        }
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean estado, String busqueda, String sortBy, String direction) {
        List<UnidadMedidaResponseDTO> unidades = mapToResponseDTOList(
                unidadMedidaRepository.findAll(construirSortUnidades(sortBy, direction)));

        List<UnidadMedidaResponseDTO> filtradas = unidades.stream()
                .filter(unidad -> estado == null || Objects.equals(unidad.getEstado(), estado))
                .filter(unidad -> coincideBusqueda(unidad, busqueda))
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Unidades de medida");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "ID", "Nombre", "Simbolo", "Tipo", "Estado", "Fecha registro"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (UnidadMedidaResponseDTO unidad : filtradas) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(unidad.getId());
                row.createCell(1).setCellValue(nvl(unidad.getNombre()));
                row.createCell(2).setCellValue(nvl(unidad.getSimbolo()));
                row.createCell(3).setCellValue(nvl(unidad.getTipo()));
                row.createCell(4).setCellValue(Boolean.TRUE.equals(unidad.getEstado()) ? "Activo" : "Inactivo");
                row.createCell(5).setCellValue(unidad.getFechaRegistro() != null ? unidad.getFechaRegistro().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el reporte de unidades de medida", e);
        }
    }

    private boolean coincideBusqueda(UnidadMedidaResponseDTO unidad, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        String.valueOf(unidad.getId()),
                        unidad.getNombre(),
                        unidad.getSimbolo(),
                        unidad.getTipo(),
                        unidad.getEstado() != null ? (unidad.getEstado() ? "activo" : "inactivo") : null,
                        unidad.getFechaRegistro() != null ? unidad.getFechaRegistro().toString() : null)
                .filter(valor -> valor != null && !valor.isBlank())
                .map(valor -> valor.toLowerCase(Locale.ROOT))
                .anyMatch(valor -> valor.contains(termino));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    public UnidadMedidaResponseDTO obtenerPorId(Long id) {

        UnidadMedidaModel UnidadMedida = unidadMedidaRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Unidad de Medida no encontrada"));

        return mapToResponseDTO(UnidadMedida);
    }

    //----------UPDATE----------
    public UnidadMedidaResponseDTO actualizar(Long id, UnidadMedidaUpdateDTO umM) {
        UnidadMedidaModel unidadMedida = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unidad de Medida no encontrada"));

        unidadMedida.setNombre(umM.getNombre());
        unidadMedida.setSimbolo(umM.getSimbolo());
        unidadMedida.setTipo(umM.getTipo());

        UnidadMedidaModel actualizado = unidadMedidaRepository.save(unidadMedida);
        return mapToResponseDTO(actualizado);
           
    }

    // --------- DELETE ---------
    public void eliminar(Long id) {
        UnidadMedidaModel unidadMedida = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unidad de Medida no encontrada"));

        unidadMedidaRepository.delete(unidadMedida);
    }   

    //------------Desactivar----------
    public boolean desactivar(Long id) {
        return unidadMedidaRepository.findById(id).map(unidadMedida -> {
            unidadMedida.setEstado(false);
            unidadMedidaRepository.save(unidadMedida);
            return true;
        }).orElse(false);   
    }

     //------------Activar----------
    public boolean activar(Long id) {
        return unidadMedidaRepository.findById(id).map(unidadMedida -> {
            unidadMedida.setEstado(true);
            unidadMedidaRepository.save(unidadMedida);
            return true;
        }).orElse(false);
    }


}
