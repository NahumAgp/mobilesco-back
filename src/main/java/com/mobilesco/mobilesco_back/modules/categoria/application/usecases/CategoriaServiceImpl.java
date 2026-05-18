/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/categoria/application/usecases/CategoriaServiceImpl.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: CategoriaServiceImpl
 * CONTEXTO: Implementacion de casos de uso del modulo; orquesta reglas de negocio y puertos de salida.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.categoria.application.usecases;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.in.api.dtos.CategoriaCreateDTO;
import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.in.api.dtos.CategoriaResponseDTO;
import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.in.api.dtos.CategoriaUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.categoria.domain.models.CategoriaModel;
import com.mobilesco.mobilesco_back.modules.categoria.application.ports.CategoriaPersistencePort;
import com.mobilesco.mobilesco_back.modules.categoria.application.usecases.CategoriaUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaUseCase {

    private final CategoriaPersistencePort categoriaRepository;

    @Transactional
    public CategoriaResponseDTO crear(CategoriaCreateDTO dto) {
        log.info("Creando nueva categoría: {}", dto.getNombre());
        
        // Validar nombre único
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        // Crear entidad
        CategoriaModel categoria = CategoriaModel.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();

        CategoriaModel saved = categoriaRepository.save(categoria);
        log.info("Categoría creada con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }

    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaUpdateDTO dto) {
        log.info("Actualizando categoría ID: {}", id);
        
        CategoriaModel categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        // Validar nombre único (excepto si es el mismo)
        if (!categoria.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        // Actualizar
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            categoria.setActivo(dto.getActivo());
        }

        CategoriaModel updated = categoriaRepository.save(categoria);
        log.info("Categoría actualizada: {}", updated.getNombre());
        
        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Long id) {
        CategoriaModel categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        return mapToResponseDTO(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean activo, String busqueda, String sortBy, String direction) {
        List<CategoriaResponseDTO> categorias = categoriaRepository.findAll(construirSortCategorias(sortBy, direction))
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        List<CategoriaResponseDTO> filtradas = categorias.stream()
                .filter(categoria -> activo == null || Objects.equals(categoria.getActivo(), activo))
                .filter(categoria -> coincideBusqueda(categoria, busqueda))
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Categorias");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "ID", "Nombre", "Descripcion", "Estado", "Registro", "Actualizacion"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (CategoriaResponseDTO categoria : filtradas) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(categoria.getId() != null ? categoria.getId() : 0L);
                row.createCell(1).setCellValue(nvl(categoria.getNombre()));
                row.createCell(2).setCellValue(nvl(categoria.getDescripcion()));
                row.createCell(3).setCellValue(Boolean.TRUE.equals(categoria.getActivo()) ? "Activo" : "Inactivo");
                row.createCell(4).setCellValue(categoria.getFechaRegistro() != null ? categoria.getFechaRegistro().toString() : "");
                row.createCell(5).setCellValue(categoria.getFechaActualizacion() != null ? categoria.getFechaActualizacion().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el reporte de categorias", e);
        }
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarActivos() {
        return categoriaRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> buscar(String nombre) {
        return categoriaRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private org.springframework.data.domain.Sort construirSortCategorias(String sortBy, String direction) {
        org.springframework.data.domain.Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? org.springframework.data.domain.Sort.Direction.DESC
                : org.springframework.data.domain.Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "nombre")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
        }

        String campo = sortBy.trim().toLowerCase(Locale.ROOT);
        return switch (campo) {
            case "id" -> org.springframework.data.domain.Sort.by(sortDirection, "id");
            case "nombre" -> org.springframework.data.domain.Sort.by(sortDirection, "nombre")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            case "descripcion" -> org.springframework.data.domain.Sort.by(sortDirection, "descripcion")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            case "activo" -> org.springframework.data.domain.Sort.by(sortDirection, "activo")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            case "fecharegistro", "fecha_registro" -> org.springframework.data.domain.Sort.by(sortDirection, "fechaRegistro")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            case "fechaactualizacion", "fecha_actualizacion" -> org.springframework.data.domain.Sort.by(sortDirection, "fechaActualizacion")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            default -> org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "nombre")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
        };
    }

    private boolean coincideBusqueda(CategoriaResponseDTO categoria, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        categoria.getId() != null ? String.valueOf(categoria.getId()) : null,
                        categoria.getNombre(),
                        categoria.getDescripcion(),
                        categoria.getActivo() != null ? (categoria.getActivo() ? "activo" : "inactivo") : null,
                        categoria.getFechaRegistro() != null ? categoria.getFechaRegistro().toString() : null,
                        categoria.getFechaActualizacion() != null ? categoria.getFechaActualizacion().toString() : null)
                .filter(valor -> valor != null && !valor.isBlank())
                .map(valor -> valor.toLowerCase(Locale.ROOT))
                .anyMatch(valor -> valor.contains(termino));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    @Transactional
    public CategoriaResponseDTO activar(Long id) {
        CategoriaModel categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        categoria.setActivo(true);
        CategoriaModel updated = categoriaRepository.save(categoria);
        return mapToResponseDTO(updated);
    }

    @Transactional
    public CategoriaResponseDTO desactivar(Long id) {
        CategoriaModel categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        categoria.setActivo(false);
        CategoriaModel updated = categoriaRepository.save(categoria);
        return mapToResponseDTO(updated);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando (desactivando) categoría ID: {}", id);
        
        CategoriaModel categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
        
        log.info("Categoría desactivada correctamente");
    }

    private CategoriaResponseDTO mapToResponseDTO(CategoriaModel categoria) {
        return CategoriaResponseDTO.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .activo(categoria.getActivo())
                .fechaRegistro(categoria.getFechaRegistro())
                .fechaActualizacion(categoria.getFechaActualizacion())
                .build();
    }
}





