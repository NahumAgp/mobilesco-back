package com.mobilesco.mobilesco_back.modules.compra.application.usecases;

import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraCreateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraUpdateDTO;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.DetalleCompraModel;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories.UnidadMedidaRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
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
    private final KardexService kardexService;

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
        
        // Una compra nueva todavia no actualiza stock; la recepcion se registra desde Entradas.
        Double cantidadRecibida = dto.getCantidadRecibida() != null ? 
                                   dto.getCantidadRecibida() : 0.0;

        if (cantidadRecibida < 0) {
            throw new ValidationException("La cantidad recibida no puede ser negativa");
        }

        if (cantidadRecibida > dto.getCantidad()) {
            throw new ValidationException("La cantidad recibida no puede ser mayor a la cantidad comprada");
        }
        
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
    public DetalleCompraResponseDTO recibirParcial(Long id, Double cantidadRecibida, String entregadoPor, String motivoNoRecepcion) {
        log.info("Registrando recepción parcial para detalle ID: {}, cantidad: {}", id, cantidadRecibida);
        
        DetalleCompraModel detalle = detalleCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado con id: " + id));
        
        if (cantidadRecibida == null || cantidadRecibida <= 0) {
            throw new ValidationException("La cantidad recibida debe ser mayor a 0");
        }

        double cantidadPendiente = detalle.getCantidadPendiente();
        if (cantidadRecibida > cantidadPendiente) {
            throw new ValidationException("La cantidad recibida no puede ser mayor a la cantidad comprada");
        }
        
        double cantidadRecibidaAnterior = detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida() : 0.0;
        double nuevaCantidadRecibida = cantidadRecibidaAnterior + cantidadRecibida;
        double cantidadConsumoDelta = cantidadRecibida * detalle.getFactorConversion();

        InsumoModel insumo = detalle.getInsumo();
        double stockAnterior = insumo.getStockActual() != null ? insumo.getStockActual() : 0.0;
        double stockNuevo = stockAnterior + cantidadConsumoDelta;

        insumo.setStockActual(stockNuevo);
        insumoRepository.save(insumo);

        detalle.setCantidadRecibida(nuevaCantidadRecibida);
        detalle.setMotivoNoRecepcion(
                nuevaCantidadRecibida < detalle.getCantidad()
                        ? motivoNoRecepcion
                        : null
        );
        DetalleCompraModel updated = detalleCompraRepository.save(detalle);

        kardexService.registrarEntradaCompra(
                insumo.getId(),
                cantidadConsumoDelta,
                detalle.getCostoPorUnidadConsumo(),
                detalle.getCompra().getNumeroDocumento(),
                detalle.getCompra().getId(),
                construirObservacionRecepcion(detalle, entregadoPor, motivoNoRecepcion)
        );

        CompraModel compra = detalle.getCompra();
        boolean compraCompleta = detalleCompraRepository.findByCompraId(compra.getId())
                .stream()
                .allMatch(det -> {
                    double recibida = det.getCantidadRecibida() != null ? det.getCantidadRecibida() : 0.0;
                    double comprada = det.getCantidad() != null ? det.getCantidad() : 0.0;
                    return recibida >= comprada;
                });

        compra.setEntregadoPor(entregadoPor != null ? entregadoPor.trim() : compra.getEntregadoPor());
        compra.setEstado(compraCompleta ? "RECIBIDA" : "RECIBIDA_PARCIAL");
        compra.setFechaRecepcion(java.time.LocalDate.now());
        compraRepository.save(compra);
        
        return mapToResponseDTO(updated);
    }

    private String construirObservacionRecepcion(DetalleCompraModel detalle, String entregadoPor, String motivoNoRecepcion) {
        StringBuilder observacion = new StringBuilder("Entrada por recepcion de compra: ")
                .append(detalle.getCompra().getFolio());

        if (entregadoPor != null && !entregadoPor.isBlank()) {
            observacion.append(" | Entregado por: ").append(entregadoPor.trim());
        }

        if (motivoNoRecepcion != null && !motivoNoRecepcion.isBlank()) {
            observacion.append(" | Motivo: ").append(motivoNoRecepcion.trim());
        }

        return observacion.length() > 255
                ? observacion.substring(0, 255)
                : observacion.toString();
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
        if ("RECIBIDA".equals(detalle.getCompra().getEstado()) || "RECIBIDA_PARCIAL".equals(detalle.getCompra().getEstado())) {
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
                .cantidadPendiente(detalle.getCantidadPendiente())
                
                // Precios
                .precioUnitario(detalle.getPrecioUnitario())
                .costoPorUnidadConsumo(detalle.getCostoPorUnidadConsumo())
                .subtotal(detalle.getSubtotal())
                
                .observaciones(detalle.getObservaciones())
                .motivoNoRecepcion(detalle.getMotivoNoRecepcion())
                .fechaRegistro(detalle.getFechaRegistro())
                .fechaActualizacion(detalle.getFechaActualizacion())
                .build();
    }
}
