package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.Insumo.InsumoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Insumo.InsumoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Insumo.InsumoUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.InsumoModel;
import com.mobilesco.mobilesco_back.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.repositories.UnidadMedidaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsumoService {

    private final InsumoRepository insumoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;

    /**
     * ACTUALIZAR un insumo existente
     */
    @Transactional
    public InsumoResponseDTO actualizar(Long id, InsumoUpdateDTO dto) {
        log.info("Actualizando insumo ID: {}", id);
        
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));

        // Validar nombre único (excepto si es el mismo)
        if (!insumo.getNombre().equalsIgnoreCase(dto.getNombre()) && 
                insumoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidationException("Ya existe un insumo con el nombre: " + dto.getNombre());
        }

        // Validar unidad de medida si cambió
        if (dto.getUnidadMedidaId() != null) {
            
            // 🔴 CORRECCIÓN 1: Obtener el ID como Long y comparar correctamente
            Long unidadActualId = insumo.getUnidadMedida().getId();
            
            // 🔴 CORRECCIÓN 2: Comparación correcta entre Long objetos
            if (!unidadActualId.equals(dto.getUnidadMedidaId())) {
                
                UnidadMedidaModel nuevaUnidad = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                            "Unidad de medida no encontrada con id: " + dto.getUnidadMedidaId()));
                
                if (!nuevaUnidad.getEstado()) {
                    throw new ValidationException("La unidad de medida está inactiva: " + nuevaUnidad.getNombre());
                }
                
                insumo.setUnidadMedida(nuevaUnidad);
            }
        }

        // Actualizar campos
        insumo.setNombre(dto.getNombre());
        insumo.setDescripcion(dto.getDescripcion());
        insumo.setUbicacion(dto.getUbicacion());
        insumo.setFila(dto.getFila());
        insumo.setColumna(dto.getColumna());
        
        if (dto.getStockMinimo() != null) {
            insumo.setStockMinimo(dto.getStockMinimo());
        }
        
        if (dto.getStockActual() != null) {
            insumo.setStockActual(dto.getStockActual());
        }
        
        if (dto.getActivo() != null) {
            insumo.setActivo(dto.getActivo());
        }

        InsumoModel updated = insumoRepository.save(insumo);
        log.info("Insumo actualizado: {}", updated.getNombre());
        
        return mapToResponseDTO(updated);
    }

    /**
     * CREAR un nuevo insumo
     */
  /**
 * CREAR un nuevo insumo
 */
@Transactional
public InsumoResponseDTO crear(InsumoCreateDTO dto) {
    log.info("Creando nuevo insumo: {}", dto.getNombre());
    
    // Validar nombre único
    if (insumoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
        throw new ValidationException("Ya existe un insumo con el nombre: " + dto.getNombre());
    }

    // Validar que la unidad de medida exista y esté activa
    UnidadMedidaModel unidadMedida = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
            .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con id: " + dto.getUnidadMedidaId()));
    
    if (!unidadMedida.getEstado()) {
        throw new ValidationException("La unidad de medida está inactiva: " + unidadMedida.getNombre());
    }

    // 🔴 CORREGIDO: Manejo seguro de valores null
    Double stockMinimo = dto.getStockMinimo();
    if (stockMinimo == null) {
        stockMinimo = 0.0;  // Usar Double, no int
    }

    // Crear entidad
    InsumoModel insumo = InsumoModel.builder()
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .ubicacion(dto.getUbicacion())
            .fila(dto.getFila())
            .columna(dto.getColumna())
            .unidadMedida(unidadMedida)
            .stockMinimo(stockMinimo)  // ✅ Ya es Double, no hay unboxing
            .stockActual(0.0)           // ✅ Double literal
            .activo(true)                // ✅ Boolean literal
            .build();

    InsumoModel saved = insumoRepository.save(insumo);
    log.info("Insumo creado con ID: {}", saved.getId());
    
    return mapToResponseDTO(saved);
}

    /**
     * OBTENER insumo por ID
     */
    @Transactional(readOnly = true)
    public InsumoResponseDTO obtenerPorId(Long id) {
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));
        return mapToResponseDTO(insumo);
    }

    /**
     * LISTAR todos los insumos
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listar() {
        return insumoRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR solo insumos activos
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listarActivos() {
        return insumoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * BUSCAR insumos por nombre
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> buscar(String nombre) {
        return insumoRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR por unidad de medida
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listarPorUnidadMedida(Long unidadMedidaId) {
        return insumoRepository.findByUnidadMedidaId(unidadMedidaId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR insumos con stock bajo
     */
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listarStockBajo() {
        return insumoRepository.findWithStockBajo()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * AJUSTAR stock manualmente
     */
    @Transactional
    public InsumoResponseDTO ajustarStock(Long id, Double cantidad, String tipo, String motivo) {
        log.info("Ajustando stock - Insumo ID: {}, Cantidad: {}, Tipo: {}", id, cantidad, tipo);
        
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));
        
        if ("ENTRADA".equalsIgnoreCase(tipo)) {
            insumo.setStockActual(insumo.getStockActual() + cantidad);
            log.info("Entrada de stock. Nuevo stock: {}", insumo.getStockActual());
            
        } else if ("SALIDA".equalsIgnoreCase(tipo)) {
            if (insumo.getStockActual() < cantidad) {
                throw new ValidationException(String.format(
                    "Stock insuficiente. Actual: %.2f %s, solicitado: %.2f %s",
                    insumo.getStockActual(), 
                    insumo.getUnidadMedida().getSimbolo(),
                    cantidad, 
                    insumo.getUnidadMedida().getSimbolo()));
            }
            insumo.setStockActual(insumo.getStockActual() - cantidad);
            log.info("Salida de stock. Nuevo stock: {}", insumo.getStockActual());
            
        } else {
            throw new ValidationException("Tipo debe ser 'ENTRADA' o 'SALIDA'");
        }
        
        InsumoModel updated = insumoRepository.save(insumo);
        return mapToResponseDTO(updated);
    }

    /**
     * ELIMINAR (desactivar) insumo
     */
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando (desactivando) insumo ID: {}", id);
        
        InsumoModel insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));
        
        insumo.setActivo(false);
        insumoRepository.save(insumo);
        
        log.info("Insumo desactivado correctamente");
    }

    /**
     * Mapear de Entity a ResponseDTO
     */
    private InsumoResponseDTO mapToResponseDTO(InsumoModel insumo) {
        return InsumoResponseDTO.builder()
                .id(insumo.getId())
                .nombre(insumo.getNombre())
                .descripcion(insumo.getDescripcion())
                .ubicacion(insumo.getUbicacion())
                .fila(insumo.getFila())
                .columna(insumo.getColumna())   
                .unidadMedidaId(insumo.getUnidadMedida().getId())
                .unidadMedidaNombre(insumo.getUnidadMedida().getNombre())
                .unidadMedidaSimbolo(insumo.getUnidadMedida().getSimbolo())
                .stockActual(insumo.getStockActual())
                .stockMinimo(insumo.getStockMinimo())
                .activo(insumo.getActivo())
                .fechaRegistro(insumo.getFechaRegistro())
                .fechaActualizacion(insumo.getFechaActualizacion())
                .build();
    }
}