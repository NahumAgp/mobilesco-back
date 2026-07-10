package com.mobilesco.mobilesco_back.modules.kardex.application.usecases;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.in.api.dtos.MovimientoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.kardex.domain.models.MovimientoInsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.out.persistence.repositories.KardexRepository;
import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KardexService {

    private final KardexRepository kardexRepository;
    private final InsumoRepository insumoRepository;
    private final CompraRepository compraRepository;

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
            String observaciones,
            Double stockAnterior,
            Double stockNuevo) {
        
        log.info("Registrando entrada por compra - Insumo ID: {}, Cantidad: {}", insumoId, cantidad);
        
        InsumoModel insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));

        Double stockAnteriorSeguro = stockAnterior != null ? stockAnterior : 0.0;
        Double stockNuevoSeguro = stockNuevo != null ? stockNuevo : stockAnteriorSeguro + cantidad;
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
                .stockAnterior(stockAnteriorSeguro)
                .stockNuevo(stockNuevoSeguro)
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
            String observaciones,
            Double stockAnterior,
            Double stockNuevo) {
        
        log.info("Registrando salida por producción - Insumo ID: {}, Cantidad: {}", insumoId, cantidad);
        
        InsumoModel insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));

        Double stockAnteriorSeguro = stockAnterior != null ? stockAnterior : 0.0;
        Double stockNuevoSeguro = stockNuevo != null ? stockNuevo : stockAnteriorSeguro - cantidad;
        if (stockNuevoSeguro < 0) {
            throw new ValidationException(String.format(
                "Stock insuficiente. Actual: %.2f %s, requerido: %.2f %s",
                stockAnteriorSeguro, insumo.getUnidadMedida().getSimbolo(),
                cantidad, insumo.getUnidadMedida().getSimbolo()));
        }

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
                .stockAnterior(stockAnteriorSeguro)
                .stockNuevo(stockNuevoSeguro)
                .produccionId(produccionId)
                .build();
        
        MovimientoInsumoModel saved = kardexRepository.save(movimiento);
        log.info("Movimiento registrado con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }

    @Transactional
    public MovimientoInsumoResponseDTO registrarReversaSalida(
            Long insumoId,
            Double cantidad,
            Double costoUnitario,
            Long salidaInsumoId,
            String observaciones,
            Double stockAnterior,
            Double stockNuevo,
            String usuario) {

        log.info("Registrando reversa de salida - Insumo ID: {}, Salida ID: {}, Cantidad: {}",
                insumoId, salidaInsumoId, cantidad);

        InsumoModel insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));

        Double cantidadSegura = cantidad != null ? cantidad : 0.0;
        Double costoSeguro = costoUnitario != null ? costoUnitario : 0.0;
        Double stockAnteriorSeguro = stockAnterior != null ? stockAnterior : 0.0;
        Double stockNuevoSeguro = stockNuevo != null ? stockNuevo : stockAnteriorSeguro + cantidadSegura;

        MovimientoInsumoModel movimiento = MovimientoInsumoModel.builder()
                .insumo(insumo)
                .fecha(LocalDateTime.now())
                .tipo("ENTRADA")
                .concepto("DEVOLUCION")
                .cantidad(cantidadSegura)
                .costoUnitario(costoSeguro)
                .costoTotal(cantidadSegura * costoSeguro)
                .referencia("Reversa salida #" + salidaInsumoId)
                .observaciones(observaciones)
                .stockAnterior(stockAnteriorSeguro)
                .stockNuevo(stockNuevoSeguro)
                .usuario(usuario)
                .produccionId(salidaInsumoId)
                .build();

        MovimientoInsumoModel saved = kardexRepository.save(movimiento);
        log.info("Reversa de salida registrada con ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    /**
     * REGISTRAR un ajuste manual
     */
    @Transactional
    public MovimientoInsumoResponseDTO registrarAjuste(
            Long insumoId,
            Double stockAnterior,
            Double nuevoStock,
            String motivo,
            String usuario) {
        
        log.info("Registrando ajuste manual - Insumo ID: {}, Nuevo stock: {}", insumoId, nuevoStock);
        
        InsumoModel insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));

        Double stockAnteriorSeguro = stockAnterior != null ? stockAnterior : 0.0;
        Double nuevoStockSeguro = nuevoStock != null ? nuevoStock : stockAnteriorSeguro;
        Double diferencia = nuevoStockSeguro - stockAnteriorSeguro;

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
                .stockAnterior(stockAnteriorSeguro)
                .stockNuevo(nuevoStockSeguro)
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

    @Transactional(readOnly = true)
    public PageResponseDTO<MovimientoInsumoResponseDTO> obtenerHistorialPorInsumoPaginado(
            Long insumoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {
        if (!insumoRepository.existsById(insumoId)) {
            throw new ResourceNotFoundException("Insumo no encontrado");
        }

        Page<MovimientoInsumoModel> movimientos = fechaInicio != null && fechaFin != null
                ? kardexRepository.findByInsumoIdAndFechaBetween(insumoId, fechaInicio, fechaFin, pageable)
                : kardexRepository.findByInsumoId(insumoId, pageable);

        Page<MovimientoInsumoResponseDTO> page = movimientos
                .map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
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

    @Transactional(readOnly = true)
    public PageResponseDTO<MovimientoInsumoResponseDTO> obtenerMovimientosPorPeriodoPaginado(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {
        Page<MovimientoInsumoResponseDTO> page = kardexRepository.findByFechaBetween(fechaInicio, fechaFin, pageable)
                .map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * OBTENER movimientos de una compra
     */
    @Transactional(readOnly = true)
    public List<MovimientoInsumoResponseDTO> obtenerMovimientosPorCompra(Long compraId) {
        return kardexRepository.findByCompraIdOrderByFechaDescIdDesc(compraId)
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
        CompraModel compra = movimiento.getCompraId() != null
                ? compraRepository.findById(movimiento.getCompraId()).orElse(null)
                : null;
        ProveedorModel proveedor = compra != null ? compra.getProveedor() : null;

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
                .proveedorId(proveedor != null ? proveedor.getId() : null)
                .proveedorNombre(obtenerNombreProveedor(proveedor))
                .produccionId(movimiento.getProduccionId())
                .ajusteId(movimiento.getAjusteId())
                .fechaRegistro(movimiento.getFechaRegistro())
                .build();
    }

    private String obtenerNombreProveedor(ProveedorModel proveedor) {
        if (proveedor == null) {
            return null;
        }

        if (proveedor.getRazonSocial() != null && !proveedor.getRazonSocial().isBlank()) {
            return proveedor.getRazonSocial();
        }

        return List.of(
                proveedor.getNombre(),
                proveedor.getApellidoPaterno(),
                proveedor.getApellidoMaterno()
        ).stream()
                .filter(valor -> valor != null && !valor.isBlank())
                .collect(Collectors.joining(" "));
    }
}
