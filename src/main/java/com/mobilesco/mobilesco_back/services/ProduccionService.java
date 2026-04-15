package com.mobilesco.mobilesco_back.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionCreateDTO;
import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionInsumoDTO;
import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionResponseDTO;
import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionTiempoDTO;
import com.mobilesco.mobilesco_back.dto.Produccion.ProduccionUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.CentroTrabajoModel;
import com.mobilesco.mobilesco_back.models.InsumoModel;
import com.mobilesco.mobilesco_back.models.OperacionModel;
import com.mobilesco.mobilesco_back.models.ProduccionInsumoModel;
import com.mobilesco.mobilesco_back.models.ProduccionModel;
import com.mobilesco.mobilesco_back.models.ProduccionTiempoModel;
import com.mobilesco.mobilesco_back.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.models.ProductoModel;
import com.mobilesco.mobilesco_back.repositories.CentroTrabajoRepository;
import com.mobilesco.mobilesco_back.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.repositories.OperacionRepository;
import com.mobilesco.mobilesco_back.repositories.ProduccionInsumoRepository;
import com.mobilesco.mobilesco_back.repositories.ProduccionRepository;
import com.mobilesco.mobilesco_back.repositories.ProduccionTiempoRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProduccionService {

    private final ProduccionRepository produccionRepository;
    private final ProduccionInsumoRepository produccionInsumoRepository;
    private final ProduccionTiempoRepository produccionTiempoRepository;
    private final ProductoRepository productoRepository;
    private final InsumoRepository insumoRepository;
    private final OperacionRepository operacionRepository;
    private final CentroTrabajoRepository centroTrabajoRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final KardexService kardexService;

    @Transactional
    public ProduccionResponseDTO crear(ProduccionCreateDTO dto) {
        log.info("Creando nueva producción con folio: {}", dto.getFolio());

        // Validar folio único
        if (produccionRepository.existsByFolio(dto.getFolio())) {
            throw new ValidationException("Ya existe una producción con el folio: " + dto.getFolio());
        }

        // Validar producto
        ProductoModel producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Crear producción
        ProduccionModel produccion = ProduccionModel.builder()
                .folio(dto.getFolio())
                .producto(producto)
                .fechaProduccion(dto.getFechaProduccion() != null ? dto.getFechaProduccion() : LocalDate.now())
                .cantidad(dto.getCantidad())
                .estado("PLANEADA")
                .observaciones(dto.getObservaciones())
                .build();

        ProduccionModel saved = produccionRepository.save(produccion);
        log.info("Producción creada con ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    @Transactional
    public ProduccionResponseDTO iniciarProduccion(Long id) {
        log.info("Iniciando producción ID: {}", id);

        ProduccionModel produccion = produccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producción no encontrada"));

        if (!"PLANEADA".equals(produccion.getEstado())) {
            throw new ValidationException("Solo se pueden iniciar producciones en estado PLANEADA");
        }

        // Verificar que haya stock suficiente
        verificarStockSuficiente(produccion);

        produccion.setEstado("EN_PROCESO");
        produccion.setFechaInicio(LocalDateTime.now());

        ProduccionModel updated = produccionRepository.save(produccion);
        return mapToResponseDTO(updated);
    }

    @Transactional
    public ProduccionResponseDTO registrarConsumoInsumo(Long produccionId, ProduccionInsumoDTO dto) {
        log.info("Registrando consumo de insumo para producción ID: {}", produccionId);

        ProduccionModel produccion = produccionRepository.findById(produccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Producción no encontrada"));

        InsumoModel insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));

        // Validar stock
        if (insumo.getStockActual() < dto.getCantidadReal()) {
            throw new ValidationException(String.format(
                "Stock insuficiente de %s. Disponible: %.2f %s, Requerido: %.2f %s",
                insumo.getNombre(),
                insumo.getStockActual(),
                insumo.getUnidadMedida().getSimbolo(),
                dto.getCantidadReal(),
                insumo.getUnidadMedida().getSimbolo()
            ));
        }

        // Obtener costo unitario actual
        Double costoUnitario = kardexService.calcularCostoPromedio(insumo.getId());

        // Crear registro de consumo
        ProduccionInsumoModel consumo = ProduccionInsumoModel.builder()
                .produccion(produccion)
                .insumo(insumo)
                .cantidadTeorica(dto.getCantidadTeorica())
                .cantidadReal(dto.getCantidadReal())
                .costoUnitario(costoUnitario)
                .costoTotal(dto.getCantidadReal() * costoUnitario)
                .build();

        @SuppressWarnings("unused")
        ProduccionInsumoModel saved = produccionInsumoRepository.save(consumo);

        // Descontar del stock
        insumo.setStockActual(insumo.getStockActual() - dto.getCantidadReal());
        insumoRepository.save(insumo);

        // Registrar en Kardex
        kardexService.registrarSalidaProduccion(
            insumo.getId(),
            dto.getCantidadReal(),
            costoUnitario,
            produccionId,
            "Consumo para producción: " + produccion.getFolio()
        );

        return mapToResponseDTO(produccion);
    }

    @Transactional
    public ProduccionResponseDTO registrarTiempoOperacion(Long produccionId, ProduccionTiempoDTO dto) {
        log.info("Registrando tiempo de operación para producción ID: {}", produccionId);

        ProduccionModel produccion = produccionRepository.findById(produccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Producción no encontrada"));

        OperacionModel operacion = operacionRepository.findById(dto.getOperacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Operación no encontrada"));

        CentroTrabajoModel centro = null;
        if (dto.getCentroTrabajoId() != null) {
            centro = centroTrabajoRepository.findById(dto.getCentroTrabajoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Centro de trabajo no encontrado"));
        }

        ProduccionTiempoModel tiempo = ProduccionTiempoModel.builder()
                .produccion(produccion)
                .operacion(operacion)
                .centroTrabajo(centro)
                .minutosTeoricos(dto.getMinutosTeoricos())
                .minutosReales(dto.getMinutosReales())
                .costoMinuto(operacion.getCostoMinuto())
                .costoTotal(dto.getMinutosReales() * operacion.getCostoMinuto())
                .build();

        produccionTiempoRepository.save(tiempo);

        return mapToResponseDTO(produccion);
    }

    @Transactional
    public ProduccionResponseDTO finalizarProduccion(Long id) {
        log.info("Finalizando producción ID: {}", id);

        ProduccionModel produccion = produccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producción no encontrada"));

        if (!"EN_PROCESO".equals(produccion.getEstado())) {
            throw new ValidationException("Solo se pueden finalizar producciones en estado EN_PROCESO");
        }

        // Calcular costos totales
        Double costoInsumos = produccionInsumoRepository.sumCostoInsumosByProduccion(id);
        Double costoMOD = produccionTiempoRepository.sumCostoMODByProduccion(id);

        costoInsumos = costoInsumos != null ? costoInsumos : 0.0;
        costoMOD = costoMOD != null ? costoMOD : 0.0;

        Double costoTotal = costoInsumos + costoMOD;
        Double costoUnitario = costoTotal / produccion.getCantidad();

        produccion.setEstado("TERMINADA");
        produccion.setFechaFin(LocalDateTime.now());
        produccion.setCostoTotal(costoTotal);
        produccion.setCostoUnitario(costoUnitario);

        ProduccionModel updated = produccionRepository.save(produccion);
        log.info("Producción finalizada. Costo total: ${}, Costo unitario: ${}", costoTotal, costoUnitario);

        return mapToResponseDTO(updated);
    }

    private void verificarStockSuficiente(ProduccionModel produccion) {
        List<ProductoInsumoModel> insumosNecesarios = productoInsumoRepository
                .findByProductoId(produccion.getProducto().getId());

        List<String> errores = new java.util.ArrayList<>();

        for (ProductoInsumoModel pi : insumosNecesarios) {
            InsumoModel insumo = pi.getInsumo();
            Double cantidadNecesaria = pi.getCantidad() * produccion.getCantidad() * 
                                      (1 + pi.getDesperdicioPorcentaje() / 100);

            if (insumo.getStockActual() < cantidadNecesaria) {
                errores.add(String.format(
                    "%s: necesita %.2f %s, tiene %.2f %s",
                    insumo.getNombre(),
                    cantidadNecesaria,
                    insumo.getUnidadMedida().getSimbolo(),
                    insumo.getStockActual(),
                    insumo.getUnidadMedida().getSimbolo()
                ));
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidationException("Stock insuficiente para iniciar producción:\n" + 
                                         String.join("\n", errores));
        }
    }

    @Transactional(readOnly = true)
    public ProduccionResponseDTO obtenerPorId(Long id) {
        ProduccionModel produccion = produccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producción no encontrada"));
        return mapToResponseDTO(produccion);
    }

    @Transactional(readOnly = true)
    public List<ProduccionResponseDTO> listar() {
        return produccionRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProduccionResponseDTO> listarPorEstado(String estado) {
        return produccionRepository.findByEstado(estado)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private ProduccionResponseDTO mapToResponseDTO(ProduccionModel produccion) {
        List<ProduccionInsumoModel> insumos = produccionInsumoRepository
                .findByProduccionId(produccion.getId());
        
        List<ProduccionTiempoModel> tiempos = produccionTiempoRepository
                .findByProduccionId(produccion.getId());

        return ProduccionResponseDTO.builder()
                .id(produccion.getId())
                .folio(produccion.getFolio())
                .productoId(produccion.getProducto().getId())
                .productoSku(produccion.getProducto().getSku())
                .productoNombre(produccion.getProducto().getNombre())
                .fechaProduccion(produccion.getFechaProduccion())
                .cantidad(produccion.getCantidad())
                .fechaInicio(produccion.getFechaInicio())
                .fechaFin(produccion.getFechaFin())
                .estado(produccion.getEstado())
                .observaciones(produccion.getObservaciones())
                .costoTotal(produccion.getCostoTotal())
                .costoUnitario(produccion.getCostoUnitario())
                .insumos(insumos.stream().map(this::mapInsumoToDTO).collect(Collectors.toList()))
                .tiempos(tiempos.stream().map(this::mapTiempoToDTO).collect(Collectors.toList()))
                .build();
    }

    private ProduccionInsumoDTO mapInsumoToDTO(ProduccionInsumoModel pi) {
        return ProduccionInsumoDTO.builder()
                .insumoId(pi.getInsumo().getId())
                .insumoNombre(pi.getInsumo().getNombre())
                .cantidadTeorica(pi.getCantidadTeorica())
                .cantidadReal(pi.getCantidadReal())
                .costoUnitario(pi.getCostoUnitario())
                .costoTotal(pi.getCostoTotal())
                .build();
    }

    private ProduccionTiempoDTO mapTiempoToDTO(ProduccionTiempoModel pt) {
        return ProduccionTiempoDTO.builder()
                .operacionId(pt.getOperacion().getId())
                .operacionNombre(pt.getOperacion().getNombre())
                .centroTrabajoId(pt.getCentroTrabajo() != null ? pt.getCentroTrabajo().getId() : null)
                .centroTrabajoNombre(pt.getCentroTrabajo() != null ? pt.getCentroTrabajo().getNombre() : null)
                .minutosTeoricos(pt.getMinutosTeoricos())
                .minutosReales(pt.getMinutosReales())
                .costoMinuto(pt.getCostoMinuto())
                .costoTotal(pt.getCostoTotal())
                .build();
    }

    @Transactional
public ProduccionResponseDTO actualizar(Long id, ProduccionUpdateDTO dto) {
    log.info("Actualizando producción ID: {}", id);

    ProduccionModel produccion = produccionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producción no encontrada con id: " + id));

    // Validar que no esté terminada o cancelada
    if ("TERMINADA".equals(produccion.getEstado()) || "CANCELADA".equals(produccion.getEstado())) {
        throw new ValidationException("No se puede actualizar una producción " + produccion.getEstado().toLowerCase());
    }

    // Validar folio único si cambió
    if (dto.getFolio() != null && !dto.getFolio().equals(produccion.getFolio())) {
        if (produccionRepository.existsByFolio(dto.getFolio())) {
            throw new ValidationException("Ya existe una producción con el folio: " + dto.getFolio());
        }
        produccion.setFolio(dto.getFolio());
    }

    // Validar producto si cambió
    if (dto.getProductoId() != null && !dto.getProductoId().equals(produccion.getProducto().getId())) {
        ProductoModel producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + dto.getProductoId()));
        produccion.setProducto(producto);
    }

    // Actualizar campos
    if (dto.getFechaProduccion() != null) {
        produccion.setFechaProduccion(dto.getFechaProduccion());
    }

    if (dto.getCantidad() != null) {
        produccion.setCantidad(dto.getCantidad());
    }

    if (dto.getEstado() != null) {
        // Validar transición de estados válida
        validarTransicionEstado(produccion.getEstado(), dto.getEstado());
        produccion.setEstado(dto.getEstado());
    }

    if (dto.getFechaInicio() != null) {
        produccion.setFechaInicio(dto.getFechaInicio());
    }

    if (dto.getFechaFin() != null) {
        produccion.setFechaFin(dto.getFechaFin());
    }

    if (dto.getObservaciones() != null) {
        produccion.setObservaciones(dto.getObservaciones());
    }

    if (dto.getCostoTotal() != null) {
        produccion.setCostoTotal(dto.getCostoTotal());
    }

    if (dto.getCostoUnitario() != null) {
        produccion.setCostoUnitario(dto.getCostoUnitario());
    }

    if (dto.getActivo() != null) {
        produccion.setActivo(dto.getActivo());
    }

    ProduccionModel updated = produccionRepository.save(produccion);
    log.info("Producción actualizada correctamente");

    return mapToResponseDTO(updated);
}

/**
 * Validar que la transición de estados sea válida
 */
private void validarTransicionEstado(String estadoActual, String nuevoEstado) {
        // Si es el mismo estado, no hay problema
        if (estadoActual.equals(nuevoEstado)) {
            return;
        }

        switch (estadoActual) {
            case "PLANEADA" -> {
                if (!"EN_PROCESO".equals(nuevoEstado) && !"CANCELADA".equals(nuevoEstado)) {
                    throw new ValidationException(
                            "Desde PLANEADA solo se puede pasar a EN_PROCESO o CANCELADA");
                }
            }
                
            case "EN_PROCESO" -> {
                if (!"TERMINADA".equals(nuevoEstado) && !"CANCELADA".equals(nuevoEstado)) {
                    throw new ValidationException(
                            "Desde EN_PROCESO solo se puede pasar a TERMINADA o CANCELADA");
                }
            }
                
            case "TERMINADA" -> throw new ValidationException("No se puede cambiar el estado de una producción TERMINADA");
                
            case "CANCELADA" -> throw new ValidationException("No se puede cambiar el estado de una producción CANCELADA");
                
            default -> throw new ValidationException("Estado no válido: " + estadoActual);
        }
    }

    @Transactional
    public ProduccionResponseDTO registrarConsumoInsumosMasivo(Long produccionId, List<ProduccionInsumoDTO> insumosDTO) {
        log.info("Registrando consumo masivo de {} insumos para producción ID: {}", insumosDTO.size(), produccionId);

        ProduccionModel produccion = produccionRepository.findById(produccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Producción no encontrada"));

        if (!"EN_PROCESO".equals(produccion.getEstado())) {
            throw new ValidationException("Solo se pueden registrar consumos en producciones EN_PROCESO");
        }

        List<String> errores = new ArrayList<>();
        List<ProduccionInsumoModel> consumosGuardados = new ArrayList<>();

        for (ProduccionInsumoDTO dto : insumosDTO) {
            try {
                // Validar insumo
                InsumoModel insumo = insumoRepository.findById(dto.getInsumoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado: " + dto.getInsumoId()));

                // Validar stock
                if (insumo.getStockActual() < dto.getCantidadReal()) {
                    errores.add(String.format("Stock insuficiente de %s: disponible %.2f %s, requerido %.2f %s",
                            insumo.getNombre(),
                            insumo.getStockActual(),
                            insumo.getUnidadMedida().getSimbolo(),
                            dto.getCantidadReal(),
                            insumo.getUnidadMedida().getSimbolo()));
                    continue;
                }

                // Obtener costo unitario actual
                Double costoUnitario = kardexService.calcularCostoPromedio(insumo.getId());

                // Crear registro de consumo
                ProduccionInsumoModel consumo = ProduccionInsumoModel.builder()
                        .produccion(produccion)
                        .insumo(insumo)
                        .cantidadTeorica(dto.getCantidadTeorica() != null ? dto.getCantidadTeorica() : dto.getCantidadReal())
                        .cantidadReal(dto.getCantidadReal())
                        .costoUnitario(costoUnitario)
                        .costoTotal(dto.getCantidadReal() * costoUnitario)
                        .build();

                ProduccionInsumoModel saved = produccionInsumoRepository.save(consumo);
                consumosGuardados.add(saved);

                // Descontar del stock
                insumo.setStockActual(insumo.getStockActual() - dto.getCantidadReal());
                insumoRepository.save(insumo);

                // Registrar en Kardex
                kardexService.registrarSalidaProduccion(
                    insumo.getId(),
                    dto.getCantidadReal(),
                    costoUnitario,
                    produccionId,
                    "Consumo para producción: " + produccion.getFolio()
                );

            } catch (Exception e) {
                errores.add("Error con insumo ID " + dto.getInsumoId() + ": " + e.getMessage());
            }
        }

        if (!errores.isEmpty()) {
            log.warn("Errores en consumo masivo: {}", errores);
            if (consumosGuardados.isEmpty()) {
                throw new ValidationException("No se pudo registrar ningún consumo: " + errores);
            }
        }

        log.info("Consumo masivo completado: {} insumos registrados correctamente, {} errores", 
                consumosGuardados.size(), errores.size());

        return mapToResponseDTO(produccion);
    }
}