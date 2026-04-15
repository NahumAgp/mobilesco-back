package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.CostoIndirecto.CostoIndirectoCreateDTO;
import com.mobilesco.mobilesco_back.dto.CostoIndirecto.CostoIndirectoResponseDTO;
import com.mobilesco.mobilesco_back.dto.CostoIndirecto.CostoIndirectoUpdateDTO;
import com.mobilesco.mobilesco_back.enums.TipoCostoIndirecto;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.CostoIndirectoModel;
import com.mobilesco.mobilesco_back.repositories.CostoIndirectoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostoIndirectoService {

    private final CostoIndirectoRepository costoIndirectoRepository;

    @Transactional
    public CostoIndirectoResponseDTO crear(CostoIndirectoCreateDTO dto) {
        log.info("Creando nuevo costo indirecto con código: {}", dto.getCodigo());

        // Validar código único
        if (costoIndirectoRepository.existsByCodigoIgnoreCase(dto.getCodigo())) {
            throw new ValidationException("Ya existe un costo indirecto con el código: " + dto.getCodigo());
        }

        // Validaciones según tipo
        if (dto.getTipo() == TipoCostoIndirecto.FIJO) {
            if (dto.getMontoMensual() == null && dto.getPorcentajeAsignado() == null) {
                throw new ValidationException("Para costos fijos debe indicar monto mensual o porcentaje asignado");
            }
        } else if (dto.getTipo() == TipoCostoIndirecto.VARIABLE) {
            if (dto.getTasaVariable() == null) {
                throw new ValidationException("Para costos variables debe indicar la tasa variable");
            }
        }

        // Crear entidad
        CostoIndirectoModel costo = CostoIndirectoModel.builder()
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .tipo(dto.getTipo())
                .baseDistribucion(dto.getBaseDistribucion())
                .montoMensual(dto.getMontoMensual())
                .porcentajeAsignado(dto.getPorcentajeAsignado())
                .tasaVariable(dto.getTasaVariable())
                .activo(true)
                .build();

        CostoIndirectoModel saved = costoIndirectoRepository.save(costo);
        log.info("Costo indirecto creado con ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    @Transactional
    public CostoIndirectoResponseDTO actualizar(Long id, CostoIndirectoUpdateDTO dto) {
        log.info("Actualizando costo indirecto ID: {}", id);

        CostoIndirectoModel costo = costoIndirectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Costo indirecto no encontrado con id: " + id));

        // Validar código único (excepto si es el mismo)
        if (!costo.getCodigo().equalsIgnoreCase(dto.getCodigo()) &&
                costoIndirectoRepository.existsByCodigoIgnoreCase(dto.getCodigo())) {
            throw new ValidationException("Ya existe un costo indirecto con el código: " + dto.getCodigo());
        }

        // Actualizar campos
        costo.setCodigo(dto.getCodigo());
        costo.setNombre(dto.getNombre());
        costo.setDescripcion(dto.getDescripcion());
        
        if (dto.getTipo() != null) {
            costo.setTipo(dto.getTipo());
        }
        
        if (dto.getBaseDistribucion() != null) {
            costo.setBaseDistribucion(dto.getBaseDistribucion());
        }
        
        if (dto.getMontoMensual() != null) {
            costo.setMontoMensual(dto.getMontoMensual());
        }
        
        if (dto.getPorcentajeAsignado() != null) {
            costo.setPorcentajeAsignado(dto.getPorcentajeAsignado());
        }
        
        if (dto.getTasaVariable() != null) {
            costo.setTasaVariable(dto.getTasaVariable());
        }
        
        if (dto.getActivo() != null) {
            costo.setActivo(dto.getActivo());
        }

        CostoIndirectoModel updated = costoIndirectoRepository.save(costo);
        log.info("Costo indirecto actualizado");

        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public CostoIndirectoResponseDTO obtenerPorId(Long id) {
        CostoIndirectoModel costo = costoIndirectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Costo indirecto no encontrado con id: " + id));
        return mapToResponseDTO(costo);
    }

    @Transactional(readOnly = true)
    public CostoIndirectoResponseDTO obtenerPorCodigo(String codigo) {
        CostoIndirectoModel costo = costoIndirectoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Costo indirecto no encontrado con código: " + codigo));
        return mapToResponseDTO(costo);
    }

    @Transactional(readOnly = true)
    public List<CostoIndirectoResponseDTO> listar() {
        return costoIndirectoRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CostoIndirectoResponseDTO> listarActivos() {
        return costoIndirectoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CostoIndirectoResponseDTO> listarPorTipo(TipoCostoIndirecto tipo) {
        return costoIndirectoRepository.findByTipo(tipo)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CostoIndirectoResponseDTO> buscar(String nombre) {
        return costoIndirectoRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando costo indirecto ID: {}", id);

        CostoIndirectoModel costo = costoIndirectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Costo indirecto no encontrado con id: " + id));

        costo.setActivo(false);
        costoIndirectoRepository.save(costo);
        log.info("Costo indirecto desactivado");
    }

    private CostoIndirectoResponseDTO mapToResponseDTO(CostoIndirectoModel costo) {
        return CostoIndirectoResponseDTO.builder()
                .id(costo.getId())
                .codigo(costo.getCodigo())
                .nombre(costo.getNombre())
                .descripcion(costo.getDescripcion())
                .tipo(costo.getTipo())
                .baseDistribucion(costo.getBaseDistribucion())
                .montoMensual(costo.getMontoMensual())
                .porcentajeAsignado(costo.getPorcentajeAsignado())
                .tasaVariable(costo.getTasaVariable())
                .activo(costo.getActivo())
                .fechaRegistro(costo.getFechaRegistro())
                .fechaActualizacion(costo.getFechaActualizacion())
                .build();
    }
}