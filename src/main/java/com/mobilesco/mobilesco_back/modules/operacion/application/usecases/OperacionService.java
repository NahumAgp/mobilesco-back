package com.mobilesco.mobilesco_back.modules.operacion.application.usecases;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.operacion.infrastructure.in.api.dtos.OperacionCreateDTO;
import com.mobilesco.mobilesco_back.modules.operacion.infrastructure.in.api.dtos.OperacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.operacion.infrastructure.in.api.dtos.OperacionUpdateDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.centrotrabajo.domain.models.CentroTrabajoModel;
import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import com.mobilesco.mobilesco_back.modules.centrotrabajo.infrastructure.out.persistence.repositories.CentroTrabajoRepository;
import com.mobilesco.mobilesco_back.modules.operacion.infrastructure.out.persistence.repositories.OperacionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperacionService {

    private final OperacionRepository operacionRepository;
    private final CentroTrabajoRepository centroTrabajoRepository;  // Lo crearemos después

   @Transactional
public OperacionResponseDTO crear(OperacionCreateDTO dto) {
    log.info("Creando nueva operación: {}", dto.getCodigo());
    
    // Validar centro de trabajo
    CentroTrabajoModel centroTrabajo = centroTrabajoRepository.findById(dto.getCentroTrabajoId())
            .orElseThrow(() -> new ResourceNotFoundException("Centro de trabajo no encontrado"));
    
    // 🔴 Si no viene tiempoOperacion, asignar 0 o lanzar error
    Double tiempoOperacion = dto.getTiempoOperacion();
    if (tiempoOperacion == null) {
        throw new ValidationException("El tiempo de operación es requerido");
    }
    
    OperacionModel operacion = OperacionModel.builder()
            .codigo(dto.getCodigo())
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .tiempoOperacion(tiempoOperacion)  // 🔴 AHORA SÍ SE ASIGNA
            .costoHora(null)
            .costoMinuto(dto.getCostoMinuto())
            .centroTrabajo(centroTrabajo)
            .activo(true)
            .build();
    
    OperacionModel saved = operacionRepository.save(operacion);
    log.info("Operación creada con ID: {}", saved.getId());
    
    return mapToResponseDTO(saved);
}

    @Transactional
    public OperacionResponseDTO actualizar(Long id, OperacionUpdateDTO dto) {
        log.info("Actualizando operación ID: {}", id);

        OperacionModel operacion = operacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operación no encontrada con id: " + id));

        // Validar código único (excepto si es el mismo)
        if (!operacion.getCodigo().equalsIgnoreCase(dto.getCodigo()) &&
                operacionRepository.existsByCodigoIgnoreCase(dto.getCodigo())) {
            throw new ValidationException("Ya existe una operación con el código: " + dto.getCodigo());
        }

        // Actualizar centro de trabajo si cambió
        if (dto.getCentroTrabajoId() != null) {
            if (operacion.getCentroTrabajo() == null || 
                !operacion.getCentroTrabajo().getId().equals(dto.getCentroTrabajoId())) {
                
                CentroTrabajoModel centroTrabajo = centroTrabajoRepository.findById(dto.getCentroTrabajoId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Centro de trabajo no encontrado con id: " + dto.getCentroTrabajoId()));
                operacion.setCentroTrabajo(centroTrabajo);
            }
        } else {
            operacion.setCentroTrabajo(null);
        }

        // Actualizar campos
        operacion.setCodigo(dto.getCodigo());
        operacion.setNombre(dto.getNombre());
        operacion.setDescripcion(dto.getDescripcion());
        operacion.setTiempoOperacion(dto.getTiempoOperacion());
        
        if (dto.getCostoMinuto() != null) {
            operacion.setCostoMinuto(dto.getCostoMinuto());
            operacion.setCostoHora(dto.getCostoMinuto() * 60);
        }
        
        if (dto.getActivo() != null) {
            operacion.setActivo(dto.getActivo());
        }

        OperacionModel updated = operacionRepository.save(operacion);
        log.info("Operación actualizada");

        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public OperacionResponseDTO obtenerPorId(Long id) {
        OperacionModel operacion = operacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operación no encontrada con id: " + id));
        return mapToResponseDTO(operacion);
    }

    @Transactional(readOnly = true)
    public OperacionResponseDTO obtenerPorCodigo(String codigo) {
        OperacionModel operacion = operacionRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Operación no encontrada con código: " + codigo));
        return mapToResponseDTO(operacion);
    }

    @Transactional(readOnly = true)
    public List<OperacionResponseDTO> listar() {
        return operacionRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<OperacionResponseDTO> listarPaginado(
            Boolean activo,
            String busqueda,
            String centroTrabajo,
            Pageable pageable) {
        Page<OperacionResponseDTO> page = operacionRepository
                .buscarPaginado(activo, normalizarFiltro(busqueda), normalizarFiltro(centroTrabajo), pageable)
                .map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<OperacionResponseDTO> listarActivos() {
        return operacionRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OperacionResponseDTO> buscar(String nombre) {
        return operacionRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando operación ID: {}", id);

        OperacionModel operacion = operacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operación no encontrada con id: " + id));

        operacion.setActivo(false);
        operacionRepository.save(operacion);
        log.info("Operación desactivada");
    }

    private OperacionResponseDTO mapToResponseDTO(OperacionModel operacion) {
        return OperacionResponseDTO.builder()
                .id(operacion.getId())
                .codigo(operacion.getCodigo())
                .nombre(operacion.getNombre())
                .descripcion(operacion.getDescripcion())
                .centroTrabajoId(operacion.getCentroTrabajo() != null ? 
                                 operacion.getCentroTrabajo().getId() : null)
                .centroTrabajoNombre(operacion.getCentroTrabajo() != null ? 
                                    operacion.getCentroTrabajo().getNombre() : null)
                .costoMinuto(operacion.getCostoMinuto())
                .costoHora(operacion.getCostoHora())
                .activo(operacion.getActivo())
                .fechaRegistro(operacion.getFechaRegistro())
                .tiempoOperacion(operacion.getTiempoOperacion())
                .fechaActualizacion(operacion.getFechaActualizacion())
                .build();
    }

    private String normalizarFiltro(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
