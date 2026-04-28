package com.mobilesco.mobilesco_back.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.proveedor.ProveedorCreateDTO;
import com.mobilesco.mobilesco_back.dto.proveedor.ProveedorResponseDTO;
import com.mobilesco.mobilesco_back.dto.proveedor.ProveedorUpdateDTO;
import com.mobilesco.mobilesco_back.enums.TipoInsumo;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.ProveedorModel;
import com.mobilesco.mobilesco_back.repositories.ProveedorRepository;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    // =====================================================
    // 🔹 MAPPER
    // =====================================================

    private ProveedorResponseDTO mapToResponseDTO(ProveedorModel proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();

        dto.setId(proveedor.getId());

        // IDENTIDAD
        dto.setRazonSocial(proveedor.getRazonSocial());
        dto.setRfc(proveedor.getRfc());

        // NOMBRE
        dto.setNombre(proveedor.getNombre());
        dto.setApellidoPaterno(proveedor.getApellidoPaterno());
        dto.setApellidoMaterno(proveedor.getApellidoMaterno());

        // DIRECCION
        dto.setEstado(proveedor.getEstado());
        dto.setCiudad(proveedor.getCiudad());
        dto.setColonia(proveedor.getColonia());
        dto.setCalle(proveedor.getCalle());
        dto.setNumeroExterior(proveedor.getNumeroExterior());
        dto.setNumeroInterior(proveedor.getNumeroInterior());
        dto.setCodigoPostal(proveedor.getCodigoPostal());

        dto.setTipoInsumo(proveedor.getTipoInsumo());

        // CONTACTO
        dto.setTelefono(proveedor.getTelefono());
        dto.setCorreo(proveedor.getCorreo());

        // FECHAS
        dto.setFechaRegistro(proveedor.getFechaRegistro());
        dto.setFechaUltimoContacto(proveedor.getFechaUltimoContacto());

        // ESTADO
        dto.setActivo(proveedor.getActivo());

        return dto;
    }

    private List<ProveedorResponseDTO> mapToResponseDTOList(List<ProveedorModel> proveedores) {
        return proveedores.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 🔹 CREATE
    // =====================================================

    public ProveedorResponseDTO crear(ProveedorCreateDTO dto) {

        proveedorRepository
            .findByRazonSocialIgnoreCase(dto.getRazonSocial())
            .ifPresent(p -> {
                throw new BadRequestException(
                        "Ya existe un proveedor con esa razón social"
                );
            });

        ProveedorModel proveedor = new ProveedorModel();

        // IDENTIDAD
        proveedor.setRazonSocial(dto.getRazonSocial());
        proveedor.setRfc(dto.getRfc());

        // NOMBRE
        proveedor.setNombre(dto.getNombre());
        proveedor.setApellidoPaterno(dto.getApellidoPaterno());
        proveedor.setApellidoMaterno(dto.getApellidoMaterno());

        // DIRECCION
        proveedor.setEstado(dto.getEstado());
        proveedor.setCiudad(dto.getCiudad());
        proveedor.setColonia(dto.getColonia());
        proveedor.setCalle(dto.getCalle());
        proveedor.setNumeroExterior(dto.getNumeroExterior());
        proveedor.setNumeroInterior(dto.getNumeroInterior());
        proveedor.setCodigoPostal(dto.getCodigoPostal());
        proveedor.setTipoInsumo(dto.getTipoInsumo());

        // CONTACTO
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setCorreo(dto.getCorreo());

        // REGLA DE NEGOCIO
        proveedor.setActivo(true);

        ProveedorModel guardado = proveedorRepository.save(proveedor);

        return mapToResponseDTO(guardado);
    }

    // =====================================================
    // 🔹 READ - Todos
    // =====================================================

    public List<ProveedorResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(proveedorRepository.findAll());
    }

    // =====================================================
    // 🔹 READ - Por ID
    // =====================================================

    public ProveedorResponseDTO obtenerPorId(Long id) {

        ProveedorModel proveedor = proveedorRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Proveedor no encontrado"));

        return mapToResponseDTO(proveedor);
    }

    // =====================================================
    // 🔹 READ - Por tipo de insumo (NUEVO)
    // =====================================================
    
    public List<ProveedorResponseDTO> getProveedoresPorTipo(TipoInsumo tipo) {
        List<ProveedorModel> proveedores = proveedorRepository.findByTipoInsumo(tipo);
        return mapToResponseDTOList(proveedores);
    }

    // =====================================================
    // 🔹 READ - Filtros unificados
    // =====================================================

    public List<ProveedorResponseDTO> buscarFiltrado(Boolean activo, TipoInsumo tipoInsumo, String busqueda) {
        boolean tieneBusqueda = busqueda != null && !busqueda.isBlank();

        if (activo == null && tipoInsumo == null && !tieneBusqueda) {
            return obtenerTodos();
        }

        return mapToResponseDTOList(
                proveedorRepository.buscarFiltrado(activo, tipoInsumo, tieneBusqueda ? busqueda.trim() : null));
    }

    public Page<ProveedorResponseDTO> buscarFiltradoPaginado(
            Boolean activo,
            TipoInsumo tipoInsumo,
            String busqueda,
            Pageable pageable
    ) {
        boolean tieneBusqueda = busqueda != null && !busqueda.isBlank();
        Page<ProveedorModel> page = proveedorRepository.buscarFiltradoPaginado(
                activo,
                tipoInsumo,
                tieneBusqueda ? busqueda.trim() : null,
                pageable);
        return page.map(this::mapToResponseDTO);
    }

    public byte[] generarReporteExcel(Boolean activo, TipoInsumo tipoInsumo, String busqueda) {
        List<ProveedorResponseDTO> proveedores = buscarFiltrado(activo, tipoInsumo, busqueda);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Proveedores");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "ID", "Razon Social", "RFC", "Contacto", "Tipo de Insumo",
                    "Correo", "Telefono", "Estado", "Ciudad", "Activo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (ProveedorResponseDTO proveedor : proveedores) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(proveedor.getId() != null ? proveedor.getId() : 0L);
                row.createCell(1).setCellValue(nvl(proveedor.getRazonSocial()));
                row.createCell(2).setCellValue(nvl(proveedor.getRfc()));
                row.createCell(3).setCellValue(construirContacto(proveedor));
                row.createCell(4).setCellValue(proveedor.getTipoInsumo() != null ? proveedor.getTipoInsumo().name() : "");
                row.createCell(5).setCellValue(nvl(proveedor.getCorreo()));
                row.createCell(6).setCellValue(nvl(proveedor.getTelefono()));
                row.createCell(7).setCellValue(nvl(proveedor.getEstado()));
                row.createCell(8).setCellValue(nvl(proveedor.getCiudad()));
                row.createCell(9).setCellValue(Boolean.TRUE.equals(proveedor.getActivo()) ? "Activo" : "Inactivo");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el reporte de proveedores", e);
        }
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private String construirContacto(ProveedorResponseDTO proveedor) {
        return String.join(" ",
                nvl(proveedor.getNombre()),
                nvl(proveedor.getApellidoPaterno()),
                nvl(proveedor.getApellidoMaterno())).trim();
    }

    // =====================================================
    // 🔹 READ - Por activo
    // =====================================================

    public List<ProveedorResponseDTO> buscarPorActivo(Boolean activo) {
        return mapToResponseDTOList(
                proveedorRepository.findByActivo(activo));
    }

    // =====================================================
    // 🔹 READ - Por nombre
    // =====================================================

    public List<ProveedorResponseDTO> buscarPorNombre(String nombre) {
        return mapToResponseDTOList(
                proveedorRepository.findByNombreContainingIgnoreCase(nombre));
    }

    // =====================================================
    // 🔹 READ - Por activo y nombre
    // =====================================================

    public List<ProveedorResponseDTO> buscarPorActivoYNombre(Boolean activo, String nombre) {
        return mapToResponseDTOList(
                proveedorRepository.findByActivoAndNombreContainingIgnoreCase(activo, nombre));
    }

    // =====================================================
    // 🔹 READ - Listado con filtros opcionales
    // =====================================================

    public List<ProveedorResponseDTO> listar(Boolean activo, String nombre) {
        boolean tieneNombre = nombre != null && !nombre.isBlank();

        if (activo != null && tieneNombre) {
            return buscarPorActivoYNombre(activo, nombre);
        }

        if (activo != null) {
            return buscarPorActivo(activo);
        }

        if (tieneNombre) {
            return buscarPorNombre(nombre);
        }

        return obtenerTodos();
    }

    // =====================================================
    // 🔹 READ - Listado paginado con filtros opcionales
    // =====================================================

    public Page<ProveedorResponseDTO> listarPaginado(Boolean activo, String nombre, Pageable pageable) {
        boolean tieneNombre = nombre != null && !nombre.isBlank();
        Page<ProveedorModel> page;

        if (activo != null && tieneNombre) {
            page = proveedorRepository.findByActivoAndNombreContainingIgnoreCase(activo, nombre, pageable);
        } else if (activo != null) {
            page = proveedorRepository.findByActivo(activo, pageable);
        } else if (tieneNombre) {
            page = proveedorRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else {
            page = proveedorRepository.findAll(pageable);
        }

        return page.map(this::mapToResponseDTO);
    }

    // =====================================================
    // 🔹 Tipos de insumo (UTILIDAD)
    // =====================================================

    public TipoInsumo[] getTodosLosTipos() {
        return TipoInsumo.values();
    }

    // =====================================================
    // 🔹 UPDATE
    // =====================================================

    public ProveedorResponseDTO actualizar(Long id, ProveedorUpdateDTO dto) {

        ProveedorModel existente = proveedorRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Proveedor no encontrado"));

        proveedorRepository
            .findByRazonSocialIgnoreCase(dto.getRazonSocial())
            .ifPresent(p -> {
                if (!p.getId().equals(id)) {
                    throw new BadRequestException(
                            "Ya existe un proveedor con esa razón social"
                    );
                }
            });

        // IDENTIDAD
        existente.setRazonSocial(dto.getRazonSocial());
        existente.setRfc(dto.getRfc());

        // NOMBRE
        existente.setNombre(dto.getNombre());
        existente.setApellidoPaterno(dto.getApellidoPaterno());
        existente.setApellidoMaterno(dto.getApellidoMaterno());

        // DIRECCION
        existente.setEstado(dto.getEstado());
        existente.setCiudad(dto.getCiudad());
        existente.setColonia(dto.getColonia());
        existente.setCalle(dto.getCalle());
        existente.setNumeroExterior(dto.getNumeroExterior());
        existente.setNumeroInterior(dto.getNumeroInterior());
        existente.setCodigoPostal(dto.getCodigoPostal());

        existente.setTipoInsumo(dto.getTipoInsumo());

        // CONTACTO
        existente.setTelefono(dto.getTelefono());
        existente.setCorreo(dto.getCorreo());

        // ESTADO
        existente.setActivo(dto.getActivo());

        ProveedorModel guardado = proveedorRepository.save(existente);

        return mapToResponseDTO(guardado);
    }

    // =====================================================
    // 🔹 DELETE
    // =====================================================

    public void eliminar(Long id) {

        if (!proveedorRepository.existsById(id)) {
            throw new NotFoundException("Proveedor no encontrado");
        }

        proveedorRepository.deleteById(id);
    }
}
