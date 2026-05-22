package com.mobilesco.mobilesco_back.modules.kardex.application.usecases;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.in.api.dtos.MovimientoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.kardex.domain.models.MovimientoInsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.out.persistence.repositories.KardexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KardexService {

    private final KardexRepository kardexRepository;
    private final InsumoRepository insumoRepository;

    /**
     * REGISTRAR una entrada por compra
     */
    @Transactional
    public MovimientoInsumoResponseDTO registrarEntradaCompra(
            Long insumoId, 
            Double cantidad, 
            Double costoUnitario,
            String documento,
            Long compraId,
            String observaciones) {
        
        log.info("Registrando entrada por compra - Insumo ID: {}, Cantidad: {}", insumoId, cantidad);
        
        InsumoModel insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        
        Double stockAnterior = insumo.getStockActual();
        Double stockNuevo = stockAnterior + cantidad;
        Double costoTotal = cantidad * costoUnitario;
        
        MovimientoInsumoModel movimiento = MovimientoInsumoModel.builder()
                .insumo(insumo)
                .fecha(LocalDateTime.now())
                .tipo("ENTRADA")
                .concepto("COMPRA")
                .cantidad(cantidad)
                .costoUnitario(costoUnitario)
                .costoTotal(costoTotal)
                .documento(documento)
                .referencia("Compra #" + compraId)
                .observaciones(observaciones)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .compraId(compraId)
                .build();
        
        MovimientoInsumoModel saved = kardexRepository.save(movimiento);
        log.info("Movimiento registrado con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }

    /**
     * REGISTRAR una salida por producción
     */
    @Transactional
    public MovimientoInsumoResponseDTO registrarSalidaProduccion(
            Long insumoId,
            Double cantidad,
            Double costoUnitario,
            Long produccionId,
            String observaciones) {
        
        log.info("Registrando salida por producción - Insumo ID: {}, Cantidad: {}", insumoId, cantidad);
        
        InsumoModel insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        
        if (insumo.getStockActual() < cantidad) {
            throw new ValidationException(String.format(
                "Stock insuficiente. Actual: %.2f %s, requerido: %.2f %s",
                insumo.getStockActual(), insumo.getUnidadMedida().getSimbolo(),
                cantidad, insumo.getUnidadMedida().getSimbolo()));
        }
        
        Double stockAnterior = insumo.getStockActual();
        Double stockNuevo = stockAnterior - cantidad;
        Double costoTotal = cantidad * costoUnitario;
        
        MovimientoInsumoModel movimiento = MovimientoInsumoModel.builder()
                .insumo(insumo)
                .fecha(LocalDateTime.now())
                .tipo("SALIDA")
                .concepto("PRODUCCION")
                .cantidad(cantidad)
                .costoUnitario(costoUnitario)
                .costoTotal(costoTotal)
                .referencia("Producción #" + produccionId)
                .observaciones(observaciones)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .produccionId(produccionId)
                .build();
        
        MovimientoInsumoModel saved = kardexRepository.save(movimiento);
        log.info("Movimiento registrado con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }

    /**
     * REGISTRAR un ajuste manual
     */
    @Transactional
    public MovimientoInsumoResponseDTO registrarAjuste(
            Long insumoId,
            Double nuevoStock,
            String motivo,
            String usuario) {
        
        log.info("Registrando ajuste manual - Insumo ID: {}, Nuevo stock: {}", insumoId, nuevoStock);
        
        InsumoModel insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        
        Double stockAnterior = insumo.getStockActual();
        Double diferencia = nuevoStock - stockAnterior;
        
        if (diferencia == 0) {
            throw new ValidationException("El nuevo stock es igual al actual. No hay cambio.");
        }
        
        String tipo = diferencia > 0 ? "ENTRADA" : "SALIDA";
        Double cantidad = Math.abs(diferencia);
        
        // Obtener último costo para el ajuste
        MovimientoInsumoModel ultimoMovimiento = kardexRepository.findUltimoMovimientoByInsumo(insumoId);
        Double costoUnitario = ultimoMovimiento != null ? ultimoMovimiento.getCostoUnitario() : 0.0;
        Double costoTotal = cantidad * costoUnitario;
        
        MovimientoInsumoModel movimiento = MovimientoInsumoModel.builder()
                .insumo(insumo)
                .fecha(LocalDateTime.now())
                .tipo(tipo)
                .concepto("AJUSTE")
                .cantidad(cantidad)
                .costoUnitario(costoUnitario)
                .costoTotal(costoTotal)
                .observaciones("Ajuste manual: " + motivo)
                .stockAnterior(stockAnterior)
                .stockNuevo(nuevoStock)
                .usuario(usuario)
                .build();
        
        MovimientoInsumoModel saved = kardexRepository.save(movimiento);
        log.info("Ajuste registrado con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }

    /**
     * OBTENER historial de un insumo
     */
    @Transactional(readOnly = true)
    public List<MovimientoInsumoResponseDTO> obtenerHistorialPorInsumo(Long insumoId) {
        if (!insumoRepository.existsById(insumoId)) {
            throw new ResourceNotFoundException("Insumo no encontrado");
        }
        
        return kardexRepository.findByInsumoIdOrderByFechaDesc(insumoId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * OBTENER movimientos por período
     */
    @Transactional(readOnly = true)
    public List<MovimientoInsumoResponseDTO> obtenerMovimientosPorPeriodo(
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin) {
        return kardexRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * OBTENER movimientos de una compra
     */
    @Transactional(readOnly = true)
    public List<MovimientoInsumoResponseDTO> obtenerMovimientosPorCompra(Long compraId) {
        return kardexRepository.findByCompraId(compraId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * CALCULAR costo promedio de un insumo
     */
    @Transactional(readOnly = true)
    public Double calcularCostoPromedio(Long insumoId) {
        Double costoPromedio = kardexRepository.calcularCostoPromedio(insumoId);
        return costoPromedio != null ? costoPromedio : 0.0;
    }

    /**
     * CALCULAR consumo en período
     */
    @Transactional(readOnly = true)
    public Double calcularConsumoEnPeriodo(
            Long insumoId, 
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin) {
        return kardexRepository.consumoEnPeriodo(insumoId, fechaInicio, fechaFin);
    }

    private MovimientoInsumoResponseDTO mapToResponseDTO(MovimientoInsumoModel movimiento) {
        return MovimientoInsumoResponseDTO.builder()
                .id(movimiento.getId())
                .insumoId(movimiento.getInsumo().getId())
                .insumoNombre(movimiento.getInsumo().getNombre())
                .insumoUnidad(movimiento.getInsumo().getUnidadMedida().getSimbolo())
                .fecha(movimiento.getFecha())
                .tipo(movimiento.getTipo())
                .concepto(movimiento.getConcepto())
                .cantidad(movimiento.getCantidad())
                .costoUnitario(movimiento.getCostoUnitario())
                .costoTotal(movimiento.getCostoTotal())
                .documento(movimiento.getDocumento())
                .referencia(movimiento.getReferencia())
                .observaciones(movimiento.getObservaciones())
                .stockAnterior(movimiento.getStockAnterior())
                .stockNuevo(movimiento.getStockNuevo())
                .usuario(movimiento.getUsuario())
                .compraId(movimiento.getCompraId())
                .produccionId(movimiento.getProduccionId())
                .ajusteId(movimiento.getAjusteId())
                .fechaRegistro(movimiento.getFechaRegistro())
                .build();
    }
}