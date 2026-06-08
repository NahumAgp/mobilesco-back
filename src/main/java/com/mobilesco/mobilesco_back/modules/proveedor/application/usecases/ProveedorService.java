package com.mobilesco.mobilesco_back.modules.proveedor.application.usecases;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos.ProveedorCreateDTO;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos.ProveedorResponseDTO;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos.ProveedorUpdateDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;
import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.out.persistence.repositories.ProveedorRepository;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.out.persistence.repositories.TipoInsumoRepository;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final TipoInsumoRepository tipoInsumoRepository;

    public ProveedorService(
            ProveedorRepository proveedorRepository,
            TipoInsumoRepository tipoInsumoRepository
    ) {
        this.proveedorRepository = proveedorRepository;
        this.tipoInsumoRepository = tipoInsumoRepository;
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

        dto.setTipoInsumo(proveedor.getTipoInsumo() != null ? proveedor.getTipoInsumo().getCodigo() : null);
        dto.setTipoInsumoNombre(proveedor.getTipoInsumo() != null ? proveedor.getTipoInsumo().getNombre() : null);

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
        proveedor.setTipoInsumo(resolverTipoInsumo(dto.getTipoInsumo()));

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
    
    public List<ProveedorResponseDTO> getProveedoresPorTipo(String tipo) {
        List<ProveedorModel> proveedores = proveedorRepository.findByTipoInsumo_CodigoIgnoreCase(tipo);
        return mapToResponseDTOList(proveedores);
    }

    // =====================================================
    // 🔹 READ - Filtros unificados
    // =====================================================

    public List<ProveedorResponseDTO> buscarFiltrado(Boolean activo, String tipoInsumo, String busqueda) {
        boolean tieneBusqueda = busqueda != null && !busqueda.isBlank();

        if (activo == null && tipoInsumo == null && !tieneBusqueda) {
            return obtenerTodos();
        }

        return mapToResponseDTOList(
                proveedorRepository.buscarFiltrado(activo, tipoInsumo, tieneBusqueda ? busqueda.trim() : null));
    }

    public Page<ProveedorResponseDTO> buscarFiltradoPaginado(
            Boolean activo,
            String tipoInsumo,
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

    public byte[] generarReporteExcel(Boolean activo, String tipoInsumo, String busqueda) {
        List<ProveedorResponseDTO> proveedores = buscarFiltrado(activo, tipoInsumo, busqueda);

        String[] headers = {
                "ID", "Razon Social", "RFC", "Contacto", "Tipo de Insumo",
                "Correo", "Telefono", "Estado", "Ciudad", "Activo"
        };

        return ExcelReportBuilder.generate(
                "Proveedores",
                "Reporte de proveedores",
                headers,
                proveedores.stream()
                        .map(proveedor -> new Object[] {
                                proveedor.getId() != null ? proveedor.getId() : 0L,
                                nvl(proveedor.getRazonSocial()),
                                nvl(proveedor.getRfc()),
                                construirContacto(proveedor),
                                nvl(proveedor.getTipoInsumoNombre()),
                                nvl(proveedor.getCorreo()),
                                nvl(proveedor.getTelefono()),
                                nvl(proveedor.getEstado()),
                                nvl(proveedor.getCiudad()),
                                Boolean.TRUE.equals(proveedor.getActivo()) ? "Activo" : "Inactivo"
                        })
                        .collect(Collectors.toList()));
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

        existente.setTipoInsumo(resolverTipoInsumo(dto.getTipoInsumo()));

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

    private TipoInsumoModel resolverTipoInsumo(String codigo) {
        if (!StringUtils.hasText(codigo)) {
            throw new BadRequestException("Selecciona un tipo de insumo");
        }

        return tipoInsumoRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new BadRequestException("El tipo de insumo seleccionado no existe"));
    }
}
