package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.CentroTrabajo.CentroTrabajoCreateDTO;
import com.mobilesco.mobilesco_back.dto.CentroTrabajo.CentroTrabajoResponseDTO;
import com.mobilesco.mobilesco_back.dto.CentroTrabajo.CentroTrabajoUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.CentroTrabajoModel;
import com.mobilesco.mobilesco_back.repositories.CentroTrabajoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CentroTrabajoService {

    private final CentroTrabajoRepository centroTrabajoRepository;

    @Transactional
    public CentroTrabajoResponseDTO crear(CentroTrabajoCreateDTO dto) {
        log.info("Creando nuevo centro de trabajo con código: {}", dto.getCodigo());

        // Validar código único
        if (centroTrabajoRepository.existsByCodigoIgnoreCase(dto.getCodigo())) {
            throw new ValidationException("Ya existe un centro de trabajo con el código: " + dto.getCodigo());
        }

        // Crear entidad
        CentroTrabajoModel centro = CentroTrabajoModel.builder()
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .costoHora(dto.getCostoHora())
                .capacidadDiaria(dto.getCapacidadDiaria())
                .unidadCapacidad(dto.getUnidadCapacidad())
                .horasDisponiblesDia(dto.getHorasDisponiblesDia())
                .activo(true)
                .build();

        CentroTrabajoModel saved = centroTrabajoRepository.save(centro);
        log.info("Centro de trabajo creado con ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    @Transactional
    public CentroTrabajoResponseDTO actualizar(Long id, CentroTrabajoUpdateDTO dto) {
        log.info("Actualizando centro de trabajo ID: {}", id);

        CentroTrabajoModel centro = centroTrabajoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de trabajo no encontrado con id: " + id));

        // Validar código único (excepto si es el mismo)
        if (!centro.getCodigo().equalsIgnoreCase(dto.getCodigo()) &&
                centroTrabajoRepository.existsByCodigoIgnoreCase(dto.getCodigo())) {
            throw new ValidationException("Ya existe un centro de trabajo con el código: " + dto.getCodigo());
        }

        // Actualizar campos
        centro.setCodigo(dto.getCodigo());
        centro.setNombre(dto.getNombre());
        centro.setDescripcion(dto.getDescripcion());
        
        if (dto.getCostoHora() != null) {
            centro.setCostoHora(dto.getCostoHora());
        }
        
        if (dto.getCapacidadDiaria() != null) {
            centro.setCapacidadDiaria(dto.getCapacidadDiaria());
        }
        
        if (dto.getUnidadCapacidad() != null) {
            centro.setUnidadCapacidad(dto.getUnidadCapacidad());
        }
        
        if (dto.getHorasDisponiblesDia() != null) {
            centro.setHorasDisponiblesDia(dto.getHorasDisponiblesDia());
        }
        
        if (dto.getActivo() != null) {
            centro.setActivo(dto.getActivo());
        }

        CentroTrabajoModel updated = centroTrabajoRepository.save(centro);
        log.info("Centro de trabajo actualizado");

        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public CentroTrabajoResponseDTO obtenerPorId(Long id) {
        CentroTrabajoModel centro = centroTrabajoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de trabajo no encontrado con id: " + id));
        return mapToResponseDTO(centro);
    }

    @Transactional(readOnly = true)
    public CentroTrabajoResponseDTO obtenerPorCodigo(String codigo) {
        CentroTrabajoModel centro = centroTrabajoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de trabajo no encontrado con código: " + codigo));
        return mapToResponseDTO(centro);
    }

    @Transactional(readOnly = true)
    public List<CentroTrabajoResponseDTO> listar() {
        return centroTrabajoRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CentroTrabajoResponseDTO> listarActivos() {
        return centroTrabajoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CentroTrabajoResponseDTO> buscar(String nombre) {
        return centroTrabajoRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando centro de trabajo ID: {}", id);

        CentroTrabajoModel centro = centroTrabajoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de trabajo no encontrado con id: " + id));

        centro.setActivo(false);
        centroTrabajoRepository.save(centro);
        log.info("Centro de trabajo desactivado");
    }

    private CentroTrabajoResponseDTO mapToResponseDTO(CentroTrabajoModel centro) {
        return CentroTrabajoResponseDTO.builder()
                .id(centro.getId())
                .codigo(centro.getCodigo())
                .nombre(centro.getNombre())
                .descripcion(centro.getDescripcion())
                .costoHora(centro.getCostoHora())
                .capacidadDiaria(centro.getCapacidadDiaria())
                .unidadCapacidad(centro.getUnidadCapacidad())
                .horasDisponiblesDia(centro.getHorasDisponiblesDia())
                .activo(centro.getActivo())
                .fechaRegistro(centro.getFechaRegistro())
                .fechaActualizacion(centro.getFechaActualizacion())
                .build();
    }
}