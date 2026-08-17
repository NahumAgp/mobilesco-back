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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
     * Reemplaza todas las partidas de una compra BORRADOR o PENDIENTE después
     * de validar la lista completa. La transacción evita dejar una compra
     * parcialmente actualizada y rechaza cualquier recepción previa.
     */
    @Transactional
    public double reemplazarDetallesEditables(Long compraId, List<DetalleCompraCreateDTO> dtos) {
        CompraModel compra = obtenerCompraEditable(compraId);
        for (DetalleCompraCreateDTO dto : dtos) {
            validarDetalleSolicitado(dto);
        }

        List<DetalleCompraModel> anteriores = detalleCompraRepository.findByCompraId(compraId);
        validarSinRecepciones(anteriores);
        Set<Long> insumoIds = anteriores.stream()
                .map(detalle -> detalle.getInsumo().getId())
                .collect(Collectors.toCollection(HashSet::new));
        dtos.stream().map(DetalleCompraCreateDTO::getInsumoId).forEach(insumoIds::add);
        List<Long> idsOrdenados = insumoIds.stream().sorted().toList();
        Map<Long, InsumoModel> insumosBloqueados = idsOrdenados.isEmpty()
                ? Map.of()
                : insumoRepository.findAllByIdForUpdate(idsOrdenados).stream()
                        .collect(Collectors.toMap(InsumoModel::getId, Function.identity()));
        for (Long insumoId : idsOrdenados) {
            if (!insumosBloqueados.containsKey(insumoId)) {
                throw new ResourceNotFoundException("Insumo no encontrado con id: " + insumoId);
            }
        }

        List<DetalleCompraModel> nuevos = new ArrayList<>();
        double subtotal = 0;

        for (DetalleCompraCreateDTO dto : dtos) {
            InsumoModel insumo = insumosBloqueados.get(dto.getInsumoId());
            if (!Boolean.TRUE.equals(insumo.getActivo())) {
                throw new ValidationException("El insumo está inactivo: " + insumo.getNombre());
            }
            UnidadMedidaModel unidadCompra = unidadMedidaRepository.findById(dto.getUnidadCompraId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Unidad de compra no encontrada con id: " + dto.getUnidadCompraId()));
            double subtotalSinRedondear = dto.getCantidad() * dto.getPrecioUnitario();
            if (!Double.isFinite(subtotalSinRedondear) || subtotalSinRedondear <= 0) {
                throw new ValidationException("El subtotal del detalle debe ser mayor a cero");
            }
            double subtotalLinea = redondear(subtotalSinRedondear);

            nuevos.add(DetalleCompraModel.builder()
                    .compra(compra)
                    .insumo(insumo)
                    .unidadCompra(unidadCompra)
                    .cantidad(dto.getCantidad())
                    .factorConversion(dto.getFactorConversion())
                    .precioUnitario(dto.getPrecioUnitario())
                    .cantidadRecibida(0.0)
                    .subtotal(subtotalLinea)
                    .observaciones(dto.getObservaciones())
                    .build());
            subtotal += subtotalLinea;
            if (!Double.isFinite(subtotal)) {
                throw new ValidationException("El subtotal de la compra excede el máximo permitido");
            }
        }

        if (!anteriores.isEmpty()) {
            detalleCompraRepository.deleteAll(anteriores);
            detalleCompraRepository.flush();
        }
        if (!nuevos.isEmpty()) {
            detalleCompraRepository.saveAll(nuevos);
        }
        return redondear(subtotal);
    }

    @Transactional
    public double recalcularSubtotalEditable(Long compraId) {
        obtenerCompraEditable(compraId);
        List<DetalleCompraModel> detalles = detalleCompraRepository.findByCompraId(compraId);
        double subtotal = calcularSubtotalValido(detalles, true);
        if (!detalles.isEmpty()) {
            detalleCompraRepository.saveAll(detalles);
        }
        return subtotal;
    }

    @Transactional(readOnly = true)
    public double calcularSubtotalValidoBorrador(Long compraId) {
        obtenerBorrador(compraId);
        return calcularSubtotalValido(detalleCompraRepository.findByCompraId(compraId), false);
    }

    /**
     * ACTUALIZAR un detalle de compra
     */
    @Transactional
    public DetalleCompraResponseDTO actualizar(Long id, DetalleCompraUpdateDTO dto) {
        log.info("Actualizando detalle ID: {}", id);
        
        DetalleCompraModel detalle = detalleCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado con id: " + id));

        if ("BORRADOR".equals(detalle.getCompra().getEstado())
                && dto.getCantidadRecibida() != null
                && Math.abs(dto.getCantidadRecibida()) > 0.000001) {
            throw new ValidationException("Un borrador no puede registrar cantidades recibidas");
        }
        
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

        if ("BORRADOR".equals(detalle.getCompra().getEstado())) {
            throw new ValidationException("Debe confirmar el borrador antes de recibir la compra");
        }
        
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
                detalle.getCompra().getFolio(),
                detalle.getCompra().getId(),
                construirObservacionRecepcion(detalle, entregadoPor, motivoNoRecepcion),
                stockAnterior,
                stockNuevo
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

    private CompraModel obtenerBorrador(Long compraId) {
        CompraModel compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compra no encontrada con id: " + compraId));
        if (!"BORRADOR".equals(compra.getEstado())) {
            throw new ValidationException("Los detalles solo pueden reemplazarse en una compra BORRADOR");
        }
        return compra;
    }

    private CompraModel obtenerCompraEditable(Long compraId) {
        CompraModel compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compra no encontrada con id: " + compraId));
        if (!Set.of("BORRADOR", "PENDIENTE").contains(compra.getEstado())) {
            throw new ValidationException("Las partidas solo pueden reemplazarse en una compra BORRADOR o PENDIENTE");
        }
        return compra;
    }

    private void validarSinRecepciones(List<DetalleCompraModel> detalles) {
        boolean existeRecepcion = detalles.stream().anyMatch(detalle ->
                Math.abs(detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida() : 0.0) > 0.000001);
        if (existeRecepcion) {
            throw new ValidationException("No se pueden reemplazar partidas que ya tienen cantidades recibidas");
        }
    }

    private void validarDetalleSolicitado(DetalleCompraCreateDTO dto) {
        if (dto == null || dto.getInsumoId() == null || dto.getUnidadCompraId() == null) {
            throw new ValidationException("Cada detalle debe indicar insumo y unidad de compra");
        }
        validarPositivoFinito(dto.getCantidad(), "cantidad");
        validarPositivoFinito(dto.getFactorConversion(), "factor de conversión");
        validarPositivoFinito(dto.getPrecioUnitario(), "precio unitario");
    }

    private double calcularSubtotalValido(List<DetalleCompraModel> detalles, boolean actualizarLineas) {
        double subtotal = 0;
        for (DetalleCompraModel detalle : detalles) {
            validarPositivoFinito(detalle.getCantidad(), "cantidad");
            validarPositivoFinito(detalle.getFactorConversion(), "factor de conversión");
            validarPositivoFinito(detalle.getPrecioUnitario(), "precio unitario");
            if (Math.abs(detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida() : 0.0) > 0.000001) {
                throw new ValidationException("Una compra editable no puede contener cantidades recibidas");
            }
            double subtotalSinRedondear = detalle.getCantidad() * detalle.getPrecioUnitario();
            if (!Double.isFinite(subtotalSinRedondear) || subtotalSinRedondear <= 0) {
                throw new ValidationException("El subtotal del detalle debe ser mayor a cero");
            }
            double subtotalLinea = redondear(subtotalSinRedondear);
            if (actualizarLineas) {
                detalle.setSubtotal(subtotalLinea);
                detalle.setCantidadRecibida(0.0);
            }
            subtotal += subtotalLinea;
            if (!Double.isFinite(subtotal)) {
                throw new ValidationException("El subtotal de la compra excede el máximo permitido");
            }
        }
        return redondear(subtotal);
    }

    private void validarPositivoFinito(Double valor, String campo) {
        if (valor == null || !Double.isFinite(valor) || valor <= 0) {
            throw new ValidationException("El campo " + campo + " debe ser mayor a cero");
        }
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
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
