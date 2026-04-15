package com.mobilesco.mobilesco_back.services;

import com.mobilesco.mobilesco_back.dto.Compra.DetalleCompraCreateDTO;
import com.mobilesco.mobilesco_back.dto.Compra.DetalleCompraResponseDTO;
import com.mobilesco.mobilesco_back.dto.Compra.DetalleCompraUpdateDTO;
import com.mobilesco.mobilesco_back.models.CompraModel;
import com.mobilesco.mobilesco_back.models.DetalleCompraModel;
import com.mobilesco.mobilesco_back.models.InsumoModel;
import com.mobilesco.mobilesco_back.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.repositories.UnidadMedidaRepository;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetalleCompraService {

    private final DetalleCompraRepository detalleCompraRepository;
    private final CompraRepository compraRepository;
    private final InsumoRepository insumoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;

    /**
     * CREAR un detalle de compra (normalmente se crean desde CompraService)
     */
    @Transactional
    public DetalleCompraResponseDTO crear(Long compraId, DetalleCompraCreateDTO dto) {
        log.info("Creando detalle para compra ID: {}", compraId);
        
        CompraModel compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con id: " + compraId));
        
        InsumoModel insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + dto.getInsumoId()));
        
        UnidadMedidaModel unidadCompra = unidadMedidaRepository.findById(dto.getUnidadCompraId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de compra no encontrada con id: " + dto.getUnidadCompraId()));
        
        // Validar factor de conversión
        if (dto.getFactorConversion() <= 0) {
            throw new ValidationException("El factor de conversión debe ser mayor a 0");
        }
        
        // Calcular subtotal si no viene
        Double subtotal = dto.getSubtotal();
        if (subtotal == null) {
            subtotal = dto.getCantidad() * dto.getPrecioUnitario();
        }
        
        // Cantidad recibida (por defecto = cantidad)
        Double cantidadRecibida = dto.getCantidadRecibida() != null ? 
                                   dto.getCantidadRecibida() : dto.getCantidad();
        
        DetalleCompraModel detalle = DetalleCompraModel.builder()
                .compra(compra)
                .insumo(insumo)
                .unidadCompra(unidadCompra)
                .cantidad(dto.getCantidad())
                .factorConversion(dto.getFactorConversion())
                .precioUnitario(dto.getPrecioUnitario())
                .cantidadRecibida(cantidadRecibida)
                .subtotal(subtotal)
                .observaciones(dto.getObservaciones())
                .build();
        
        DetalleCompraModel saved = detalleCompraRepository.save(detalle);
        log.info("Detalle creado con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }

    /**
     * ACTUALIZAR un detalle de compra
     */
    @Transactional
    public DetalleCompraResponseDTO actualizar(Long id, DetalleCompraUpdateDTO dto) {
        log.info("Actualizando detalle ID: {}", id);
        
        DetalleCompraModel detalle = detalleCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado con id: " + id));
        
        if (dto.getUnidadCompraId() != null) {
            UnidadMedidaModel unidadCompra = unidadMedidaRepository.findById(dto.getUnidadCompraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidad de compra no encontrada"));
            detalle.setUnidadCompra(unidadCompra);
        }
        
        if (dto.getCantidad() != null) {
            detalle.setCantidad(dto.getCantidad());
        }
        
        if (dto.getFactorConversion() != null) {
            if (dto.getFactorConversion() <= 0) {
                throw new ValidationException("El factor de conversión debe ser mayor a 0");
            }
            detalle.setFactorConversion(dto.getFactorConversion());
        }
        
        if (dto.getPrecioUnitario() != null) {
            detalle.setPrecioUnitario(dto.getPrecioUnitario());
        }
        
        if (dto.getCantidadRecibida() != null) {
            detalle.setCantidadRecibida(dto.getCantidadRecibida());
        }
        
        if (dto.getSubtotal() != null) {
            detalle.setSubtotal(dto.getSubtotal());
        } else if (dto.getCantidad() != null || dto.getPrecioUnitario() != null) {
            // Recalcular subtotal si cambiaron cantidad o precio
            Double cant = dto.getCantidad() != null ? dto.getCantidad() : detalle.getCantidad();
            Double precio = dto.getPrecioUnitario() != null ? dto.getPrecioUnitario() : detalle.getPrecioUnitario();
            detalle.setSubtotal(cant * precio);
        }
        
        if (dto.getObservaciones() != null) {
            detalle.setObservaciones(dto.getObservaciones());
        }
        
        DetalleCompraModel updated = detalleCompraRepository.save(detalle);
        return mapToResponseDTO(updated);
    }

    /**
     * OBTENER detalle por ID
     */
    @Transactional(readOnly = true)
    public DetalleCompraResponseDTO obtenerPorId(Long id) {
        DetalleCompraModel detalle = detalleCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado con id: " + id));
        return mapToResponseDTO(detalle);
    }

    /**
     * LISTAR detalles de una compra
     */
    @Transactional(readOnly = true)
    public List<DetalleCompraResponseDTO> listarPorCompra(Long compraId) {
        return detalleCompraRepository.findByCompraId(compraId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR compras de un insumo
     */
    @Transactional(readOnly = true)
    public List<DetalleCompraResponseDTO> listarPorInsumo(Long insumoId) {
        return detalleCompraRepository.findByInsumoId(insumoId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * RECIBIR parcialmente una compra (actualizar cantidad recibida)
     */
    @Transactional
    public DetalleCompraResponseDTO recibirParcial(Long id, Double cantidadRecibida) {
        log.info("Registrando recepción parcial para detalle ID: {}, cantidad: {}", id, cantidadRecibida);
        
        DetalleCompraModel detalle = detalleCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado con id: " + id));
        
        if (cantidadRecibida > detalle.getCantidad()) {
            throw new ValidationException("La cantidad recibida no puede ser mayor a la cantidad comprada");
        }
        
        detalle.setCantidadRecibida(cantidadRecibida);
        DetalleCompraModel updated = detalleCompraRepository.save(detalle);
        
        // Aquí se debería actualizar el stock del insumo
        // insumoService.actualizarStock(detalle.getInsumo().getId(), cantidadRecibida * detalle.getFactorConversion(), "ENTRADA");
        
        return mapToResponseDTO(updated);
    }

    /**
     * ELIMINAR un detalle (solo si la compra no está recibida)
     */
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando detalle ID: {}", id);
        
        DetalleCompraModel detalle = detalleCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado con id: " + id));
        
        // Validar que la compra no esté recibida
        if ("RECIBIDA".equals(detalle.getCompra().getEstado())) {
            throw new ValidationException("No se puede eliminar un detalle de una compra ya recibida");
        }
        
        detalleCompraRepository.delete(detalle);
        log.info("Detalle eliminado correctamente");
    }

    /**
     * Mapear de Entity a ResponseDTO
     */
    private DetalleCompraResponseDTO mapToResponseDTO(DetalleCompraModel detalle) {
        return DetalleCompraResponseDTO.builder()
                .id(detalle.getId())
                .compraId(detalle.getCompra().getId())
                
                // Insumo
                .insumoId(detalle.getInsumo().getId())
                .insumoNombre(detalle.getInsumo().getNombre())
                .insumoDescripcion(detalle.getInsumo().getDescripcion())
                
                // Unidad de consumo (del insumo)
                .unidadConsumoId(detalle.getInsumo().getUnidadMedida().getId())
                .unidadConsumoNombre(detalle.getInsumo().getUnidadMedida().getNombre())
                .unidadConsumoSimbolo(detalle.getInsumo().getUnidadMedida().getSimbolo())
                
                // Unidad de compra (de este detalle)
                .unidadCompraId(detalle.getUnidadCompra().getId())
                .unidadCompraNombre(detalle.getUnidadCompra().getNombre())
                .unidadCompraSimbolo(detalle.getUnidadCompra().getSimbolo())
                
                // Cantidades
                .cantidad(detalle.getCantidad())
                .factorConversion(detalle.getFactorConversion())
                .cantidadRecibida(detalle.getCantidadRecibida())
                .cantidadEnUnidadConsumo(detalle.getCantidadEnUnidadConsumo())
                
                // Precios
                .precioUnitario(detalle.getPrecioUnitario())
                .costoPorUnidadConsumo(detalle.getCostoPorUnidadConsumo())
                .subtotal(detalle.getSubtotal())
                
                .observaciones(detalle.getObservaciones())
                .fechaRegistro(detalle.getFechaRegistro())
                .fechaActualizacion(detalle.getFechaActualizacion())
                .build();
    }
}