package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

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