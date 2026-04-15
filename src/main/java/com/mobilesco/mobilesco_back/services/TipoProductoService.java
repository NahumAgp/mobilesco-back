package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.TipoProducto.TipoProductoCreateDTO;
import com.mobilesco.mobilesco_back.dto.TipoProducto.TipoProductoResponseDTO;
import com.mobilesco.mobilesco_back.dto.TipoProducto.TipoProductoUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.FamiliaModel;
import com.mobilesco.mobilesco_back.models.TipoProductoModel;
import com.mobilesco.mobilesco_back.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.repositories.TipoProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TipoProductoService {

    private final TipoProductoRepository tipoProductoRepository;
    private final FamiliaRepository familiaRepository;

    @Transactional
    public TipoProductoResponseDTO crear(TipoProductoCreateDTO dto) {
        // Validar nombre único
        if (tipoProductoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe un tipo de producto con el nombre: " + dto.getNombre());
        }

        // Validar que la familia exista
        FamiliaModel familia = familiaRepository.findById(dto.getFamiliaId())
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + dto.getFamiliaId()));

        // Crear entidad
        TipoProductoModel tipoProducto = TipoProductoModel.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .familia(familia)
                .activo(true)
                .build();

        TipoProductoModel saved = tipoProductoRepository.save(tipoProducto);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public TipoProductoResponseDTO actualizar(Long id, TipoProductoUpdateDTO dto) {
        TipoProductoModel tipoProducto = tipoProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de producto no encontrado con id: " + id));

        // Validar nombre único (excepto si es el mismo)
        if (!tipoProducto.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                tipoProductoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe un tipo de producto con el nombre: " + dto.getNombre());
        }

        // Validar que la familia exista
        FamiliaModel familia = familiaRepository.findById(dto.getFamiliaId())
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + dto.getFamiliaId()));

        // Actualizar
        tipoProducto.setNombre(dto.getNombre());
        tipoProducto.setDescripcion(dto.getDescripcion());
        tipoProducto.setFamilia(familia);
        if (dto.getActivo() != null) {
            tipoProducto.setActivo(dto.getActivo());
        }

        TipoProductoModel updated = tipoProductoRepository.save(tipoProducto);
        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public TipoProductoResponseDTO obtenerPorId(Long id) {
        TipoProductoModel tipoProducto = tipoProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de producto no encontrado con id: " + id));
        return mapToResponseDTO(tipoProducto);
    }

    @Transactional(readOnly = true)
    public List<TipoProductoResponseDTO> listar() {
        return tipoProductoRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TipoProductoResponseDTO> listarActivos() {
        return tipoProductoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TipoProductoResponseDTO> listarPorFamilia(Long familiaId) {
        // Verificar que la familia exista
        if (!familiaRepository.existsById(familiaId)) {
            throw new ResourceNotFoundException("Familia no encontrada con id: " + familiaId);
        }
        
        return tipoProductoRepository.findByFamiliaIdAndActivoTrue(familiaId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TipoProductoResponseDTO> buscar(String nombre) {
        return tipoProductoRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id) {
        TipoProductoModel tipoProducto = tipoProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de producto no encontrado con id: " + id));
        
        // Soft delete
        tipoProducto.setActivo(false);
        tipoProductoRepository.save(tipoProducto);
    }

    private TipoProductoResponseDTO mapToResponseDTO(TipoProductoModel tipoProducto) {
        // Construir ruta de familia (opcional)
        String familiaRuta = construirRutaFamilia(tipoProducto.getFamilia());
        
        return TipoProductoResponseDTO.builder()
                .id(tipoProducto.getId())
                .nombre(tipoProducto.getNombre())
                .descripcion(tipoProducto.getDescripcion())
                .familiaId(tipoProducto.getFamilia().getId())
                .familiaNombre(tipoProducto.getFamilia().getNombre())
                .familiaRuta(familiaRuta)
                .activo(tipoProducto.getActivo())
                .fechaRegistro(tipoProducto.getFechaRegistro())
                .fechaActualizacion(tipoProducto.getFechaActualizacion())
                .build();
    }

    private String construirRutaFamilia(FamiliaModel familia) {
        if (familia == null) return "";
        
        StringBuilder ruta = new StringBuilder(familia.getNombre());
        FamiliaModel padre = familia.getPadre();
        
        while (padre != null) {
            ruta.insert(0, padre.getNombre() + " > ");
            padre = padre.getPadre();
        }
        
        return ruta.toString();
    }
}