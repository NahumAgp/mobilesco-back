package com.mobilesco.mobilesco_back.modules.abastecimiento.application.usecases;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.CompraBorradorCreadaDTO;
import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.ComprasBorradorResponseDTO;
import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.CrearComprasBorradorRequestDTO;
import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.ProveedorAbastecimientoDTO;
import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.SeleccionSugerenciaDTO;
import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.SugerenciaAbastecimientoDTO;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.DetalleCompraModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.insumo.application.usecases.InsumoService;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.out.persistence.repositories.KardexRepository;
import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.out.persistence.repositories.ProveedorRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AbastecimientoService {

    static final int PERIODO_CONSUMO_DIAS = 90;
    private static final double MESES_PERIODO = PERIODO_CONSUMO_DIAS / 30.0;
    private static final String ESTADO_BORRADOR = "BORRADOR";

    private final InsumoService insumoService;
    private final InsumoRepository insumoRepository;
    private final ProveedorRepository proveedorRepository;
    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final KardexRepository kardexRepository;

    @Transactional(readOnly = true)
    public List<SugerenciaAbastecimientoDTO> obtenerSugerencias() {
        List<InsumoResponseDTO> insumos = insumoService.listarActivos();
        if (insumos.isEmpty()) {
            return List.of();
        }

        List<Long> insumoIds = insumos.stream().map(InsumoResponseDTO::getId).toList();
        LocalDateTime hasta = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime desde = hasta.minusDays(PERIODO_CONSUMO_DIAS);

        Map<Long, Double> consumos = filasAMapa(
                kardexRepository.consumoPorInsumosEnPeriodo(insumoIds, desde, hasta));
        Map<Long, Double> cantidadesAbiertas = filasAMapa(
                detalleCompraRepository.cantidadPendientePorInsumos(insumoIds));
        Map<Long, List<DetalleCompraModel>> historial = detalleCompraRepository
                .findHistorialAbastecimiento(insumoIds)
                .stream()
                .collect(Collectors.groupingBy(
                        detalle -> detalle.getInsumo().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<ProveedorModel> proveedoresActivos = proveedorRepository.findByActivo(true);

        return insumos.stream()
                .map(insumo -> construirSugerencia(
                        insumo,
                        consumos.getOrDefault(insumo.getId(), 0.0),
                        cantidadesAbiertas.getOrDefault(insumo.getId(), 0.0),
                        historial.getOrDefault(insumo.getId(), List.of()),
                        proveedoresActivos))
                .filter(sugerencia -> sugerencia != null)
                .sorted(Comparator
                        .comparingInt((SugerenciaAbastecimientoDTO sugerencia) -> ordenPrioridad(sugerencia.getPrioridad()))
                        .thenComparing(SugerenciaAbastecimientoDTO::getClasificacionAbc)
                        .thenComparing(SugerenciaAbastecimientoDTO::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public ComprasBorradorResponseDTO crearComprasBorrador(CrearComprasBorradorRequestDTO request) {
        List<SeleccionSugerenciaDTO> selecciones = request.getSugerencias();
        validarSeleccionesUnicas(selecciones);

        Set<Long> insumoIds = selecciones.stream()
                .map(SeleccionSugerenciaDTO::getInsumoId)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, InsumoModel> insumos = bloquearInsumosEnOrden(insumoIds);
        validarInsumosActivos(insumos);

        Map<Long, SugerenciaAbastecimientoDTO> sugerenciasActuales = obtenerSugerencias().stream()
                .collect(Collectors.toMap(SugerenciaAbastecimientoDTO::getInsumoId, Function.identity()));
        validarSeleccionesContraSugerenciasActuales(selecciones, sugerenciasActuales);

        Set<Long> proveedorIds = selecciones.stream()
                .map(SeleccionSugerenciaDTO::getProveedorId)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, ProveedorModel> proveedores = proveedorRepository.findAllById(proveedorIds).stream()
                .collect(Collectors.toMap(ProveedorModel::getId, Function.identity()));

        validarProveedoresSeleccionados(proveedorIds, proveedores);

        Map<ProveedorInsumoKey, DetalleCompraModel> ultimasCompras = new HashMap<>();
        detalleCompraRepository.findHistorialAbastecimiento(insumoIds).forEach(detalle -> {
            ProveedorInsumoKey key = new ProveedorInsumoKey(
                    detalle.getInsumo().getId(),
                    detalle.getCompra().getProveedor().getId());
            ultimasCompras.merge(key, detalle, this::detalleMasReciente);
        });

        Map<Long, List<SeleccionSugerenciaDTO>> seleccionesPorProveedor = selecciones.stream()
                .collect(Collectors.groupingBy(
                        SeleccionSugerenciaDTO::getProveedorId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<CompraBorradorCreadaDTO> comprasCreadas = new ArrayList<>();
        for (Map.Entry<Long, List<SeleccionSugerenciaDTO>> grupo : seleccionesPorProveedor.entrySet()) {
            ProveedorModel proveedor = proveedores.get(grupo.getKey());
            comprasCreadas.add(crearBorradorProveedor(
                    proveedor,
                    grupo.getValue(),
                    insumos,
                    ultimasCompras));
        }

        return ComprasBorradorResponseDTO.builder()
                .cantidadCompras(comprasCreadas.size())
                .cantidadPartidas(selecciones.size())
                .compras(comprasCreadas)
                .build();
    }

    private SugerenciaAbastecimientoDTO construirSugerencia(
            InsumoResponseDTO insumo,
            double consumoPeriodo,
            double cantidadAbierta,
            List<DetalleCompraModel> historial,
            List<ProveedorModel> proveedoresActivos) {
        double consumoMensual = Math.max(0, consumoPeriodo) / MESES_PERIODO;
        double stockDisponible = calcularStockDisponibleReal(insumo);
        double stockMinimo = Math.max(0, valor(insumo.getStockMinimo()));
        String clasificacionAbc = normalizarClasificacion(insumo.getClasificacionAbc());
        double puntoReorden = Math.max(stockMinimo, consumoMensual);
        double mesesCobertura = mesesCobertura(clasificacionAbc);
        double stockObjetivo = Math.max(stockMinimo * 2, consumoMensual * mesesCobertura);
        double cantidadSugerida = stockObjetivo - stockDisponible - Math.max(0, cantidadAbierta);

        if (stockObjetivo <= 0 || stockDisponible > puntoReorden || cantidadSugerida <= 0.000001) {
            return null;
        }

        List<ProveedorAbastecimientoDTO> proveedores = construirProveedores(
                insumo,
                historial,
                proveedoresActivos);
        ProveedorAbastecimientoDTO proveedorSugerido = proveedores.isEmpty() ? null : proveedores.get(0);
        String prioridad = calcularPrioridad(clasificacionAbc, stockDisponible, stockMinimo);

        return SugerenciaAbastecimientoDTO.builder()
                .insumoId(insumo.getId())
                .codigo(insumo.getCodigo())
                .nombre(insumo.getNombre())
                .unidadMedidaId(insumo.getUnidadMedidaId())
                .unidadMedidaSimbolo(insumo.getUnidadMedidaSimbolo())
                .clasificacionAbc(clasificacionAbc)
                .consumoMensual(redondear(consumoMensual, 3))
                .stockDisponible(redondear(stockDisponible, 3))
                .stockMinimo(redondear(stockMinimo, 3))
                .puntoReorden(redondear(puntoReorden, 3))
                .cantidadSugerida(redondear(cantidadSugerida, 3))
                .prioridad(prioridad)
                .explicacion(construirExplicacion(
                        clasificacionAbc,
                        stockDisponible,
                        stockMinimo,
                        consumoMensual,
                        mesesCobertura,
                        cantidadAbierta,
                        insumo.getUnidadMedidaSimbolo(),
                        proveedorSugerido))
                .proveedorSugerido(proveedorSugerido)
                .proveedores(proveedores)
                .build();
    }

    private List<ProveedorAbastecimientoDTO> construirProveedores(
            InsumoResponseDTO insumo,
            List<DetalleCompraModel> historial,
            List<ProveedorModel> proveedoresActivos) {
        Map<Long, List<DetalleCompraModel>> porProveedor = historial.stream()
                .filter(detalle -> Boolean.TRUE.equals(detalle.getCompra().getProveedor().getActivo()))
                .collect(Collectors.groupingBy(
                        detalle -> detalle.getCompra().getProveedor().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<Long, ProveedorAbastecimientoDTO> candidatos = porProveedor.values().stream()
                .map(this::mapearProveedorConHistorial)
                .collect(Collectors.toMap(
                        ProveedorAbastecimientoDTO::getId,
                        Function.identity(),
                        (primero, segundo) -> primero,
                        LinkedHashMap::new));

        String tipoInsumo = insumo.getTipoInsumo() == null ? null : insumo.getTipoInsumo().name();
        if (tipoInsumo != null) {
            proveedoresActivos.stream()
                    .filter(proveedor -> proveedor.getTipoInsumo() != null
                            && tipoInsumo.equalsIgnoreCase(proveedor.getTipoInsumo().getCodigo()))
                    .map(proveedor -> mapearProveedorSinHistorial(proveedor, insumo))
                    .forEach(proveedor -> candidatos.putIfAbsent(proveedor.getId(), proveedor));
        }

        return candidatos.values().stream()
                .sorted(comparadorProveedores())
                .toList();
    }

    private ProveedorAbastecimientoDTO mapearProveedorConHistorial(List<DetalleCompraModel> detalles) {
        DetalleCompraModel ultima = detalles.stream()
                .max(comparadorDetallePorRecencia())
                .orElseThrow();
        ProveedorModel proveedor = ultima.getCompra().getProveedor();
        long numeroCompras = detalles.stream()
                .map(detalle -> detalle.getCompra().getId())
                .distinct()
                .count();
        LocalDate ultimaCompra = detalles.stream()
                .map(detalle -> detalle.getCompra().getFechaCompra())
                .filter(fecha -> fecha != null)
                .max(LocalDate::compareTo)
                .orElse(null);
        double puntajeFrecuencia = Math.min(50, 10 + numeroCompras * 10);
        double puntajeCalificacion = proveedor.getCalificacionProveedor() == null
                ? 0
                : limitar(proveedor.getCalificacionProveedor().doubleValue(), 0, 100) * 0.4;
        double puntajeRecencia = puntajeRecencia(ultimaCompra);

        return ProveedorAbastecimientoDTO.builder()
                .id(proveedor.getId())
                .nombre(nombreProveedor(proveedor))
                .calificacion(proveedor.getCalificacionProveedor())
                .numeroCompras((int) numeroCompras)
                .ultimaCompra(ultimaCompra)
                .costoUnitario(ultima.getPrecioUnitario())
                .factorConversion(ultima.getFactorConversion())
                .unidadCompraId(ultima.getUnidadCompra().getId())
                .unidadCompraSimbolo(ultima.getUnidadCompra().getSimbolo())
                .puntaje(redondear(puntajeFrecuencia + puntajeCalificacion + puntajeRecencia, 2))
                .build();
    }

    private ProveedorAbastecimientoDTO mapearProveedorSinHistorial(
            ProveedorModel proveedor,
            InsumoResponseDTO insumo) {
        double puntaje = proveedor.getCalificacionProveedor() == null
                ? 0
                : limitar(proveedor.getCalificacionProveedor().doubleValue(), 0, 100) * 0.4;
        return ProveedorAbastecimientoDTO.builder()
                .id(proveedor.getId())
                .nombre(nombreProveedor(proveedor))
                .calificacion(proveedor.getCalificacionProveedor())
                .numeroCompras(0)
                .costoUnitario(insumo.getCostoCotizacion())
                .factorConversion(1.0)
                .unidadCompraId(insumo.getUnidadMedidaId())
                .unidadCompraSimbolo(insumo.getUnidadMedidaSimbolo())
                .puntaje(redondear(puntaje, 2))
                .build();
    }

    private CompraBorradorCreadaDTO crearBorradorProveedor(
            ProveedorModel proveedor,
            List<SeleccionSugerenciaDTO> selecciones,
            Map<Long, InsumoModel> insumos,
            Map<ProveedorInsumoKey, DetalleCompraModel> ultimasCompras) {
        CompraModel compra = CompraModel.builder()
                .folio(generarFolioBorrador(proveedor.getId()))
                .fechaCompra(LocalDate.now())
                .proveedor(proveedor)
                .tipoDocumento(ESTADO_BORRADOR)
                .subtotal(0.0)
                .impuesto(0.0)
                .total(0.0)
                .observaciones("Generada por abastecimiento asistido; cantidades y precios sujetos a revisión.")
                .estado(ESTADO_BORRADOR)
                .activo(true)
                .build();
        compra = compraRepository.save(compra);

        List<DetalleCompraModel> detalles = new ArrayList<>();
        double subtotal = 0;
        for (SeleccionSugerenciaDTO seleccion : selecciones) {
            InsumoModel insumo = insumos.get(seleccion.getInsumoId());
            DetalleCompraModel ultimaCompra = ultimasCompras.get(new ProveedorInsumoKey(
                    seleccion.getInsumoId(), proveedor.getId()));
            double factorConversion = ultimaCompra != null && valor(ultimaCompra.getFactorConversion()) > 0
                    ? ultimaCompra.getFactorConversion()
                    : 1.0;
            UnidadMedidaModel unidadCompra = ultimaCompra != null
                    ? ultimaCompra.getUnidadCompra()
                    : insumo.getUnidadMedida();
            double precioUnitario = ultimaCompra != null
                    ? Math.max(0, valor(ultimaCompra.getPrecioUnitario()))
                    : Math.max(0, valor(insumo.getCostoCotizacion()));
            double cantidadCompra = Math.max(
                    0.000001,
                    redondear(seleccion.getCantidad() / factorConversion, 6));
            double subtotalLinea = redondear(cantidadCompra * precioUnitario, 2);

            detalles.add(DetalleCompraModel.builder()
                    .compra(compra)
                    .insumo(insumo)
                    .unidadCompra(unidadCompra)
                    .cantidad(cantidadCompra)
                    .factorConversion(factorConversion)
                    .precioUnitario(precioUnitario)
                    .cantidadRecibida(0.0)
                    .subtotal(subtotalLinea)
                    .observaciones("Sugerencia: " + redondear(seleccion.getCantidad(), 3)
                            + " " + insumo.getUnidadMedida().getSimbolo() + " en unidad de consumo")
                    .build());
            subtotal += subtotalLinea;
        }

        detalleCompraRepository.saveAll(detalles);
        compra.getDetalles().addAll(detalles);
        compra.setSubtotal(redondear(subtotal, 2));
        compra.setTotal(redondear(subtotal, 2));
        compra = compraRepository.save(compra);

        return CompraBorradorCreadaDTO.builder()
                .compraId(compra.getId())
                .folio(compra.getFolio())
                .proveedorId(proveedor.getId())
                .proveedorNombre(nombreProveedor(proveedor))
                .estado(compra.getEstado())
                .partidas(detalles.size())
                .subtotalEstimado(compra.getSubtotal())
                .build();
    }

    private void validarSeleccionesUnicas(List<SeleccionSugerenciaDTO> selecciones) {
        Set<Long> insumos = new HashSet<>();
        for (SeleccionSugerenciaDTO seleccion : selecciones) {
            if (seleccion == null
                    || seleccion.getInsumoId() == null
                    || seleccion.getProveedorId() == null) {
                throw new ValidationException("Cada sugerencia debe indicar insumo y proveedor");
            }
            if (seleccion.getCantidad() == null
                    || !Double.isFinite(seleccion.getCantidad())
                    || seleccion.getCantidad() <= 0) {
                throw new ValidationException("La cantidad seleccionada debe ser un número positivo y finito");
            }
            if (!insumos.add(seleccion.getInsumoId())) {
                throw new ValidationException(
                        "El insumo " + seleccion.getInsumoId() + " está seleccionado más de una vez");
            }
        }
    }

    private Map<Long, InsumoModel> bloquearInsumosEnOrden(Set<Long> insumoIds) {
        List<Long> idsOrdenados = insumoIds.stream().sorted().toList();
        Map<Long, InsumoModel> bloqueados = insumoRepository.findAllByIdForUpdate(idsOrdenados).stream()
                .collect(Collectors.toMap(
                        InsumoModel::getId,
                        Function.identity(),
                        (primero, segundo) -> primero,
                        LinkedHashMap::new));
        for (Long insumoId : idsOrdenados) {
            if (!bloqueados.containsKey(insumoId)) {
                throw new ResourceNotFoundException("Insumo no encontrado con id: " + insumoId);
            }
        }
        return bloqueados;
    }

    private void validarInsumosActivos(Map<Long, InsumoModel> insumos) {
        for (InsumoModel insumo : insumos.values()) {
            if (!Boolean.TRUE.equals(insumo.getActivo())) {
                throw new ValidationException("El insumo está inactivo: " + insumo.getNombre());
            }
        }
    }

    private void validarSeleccionesContraSugerenciasActuales(
            List<SeleccionSugerenciaDTO> selecciones,
            Map<Long, SugerenciaAbastecimientoDTO> sugerenciasActuales) {
        for (SeleccionSugerenciaDTO seleccion : selecciones) {
            SugerenciaAbastecimientoDTO actual = sugerenciasActuales.get(seleccion.getInsumoId());
            if (actual == null) {
                throw new ValidationException(
                        "El insumo " + seleccion.getInsumoId()
                                + " ya no requiere abastecimiento; actualiza las sugerencias");
            }
            if (seleccion.getCantidad() - actual.getCantidadSugerida() > 0.0005) {
                throw new ValidationException(
                        "La cantidad del insumo " + seleccion.getInsumoId()
                                + " supera la sugerencia actual de " + actual.getCantidadSugerida());
            }
            boolean proveedorDisponible = actual.getProveedores() != null
                    && actual.getProveedores().stream()
                            .anyMatch(proveedor -> proveedor.getId().equals(seleccion.getProveedorId()));
            if (!proveedorDisponible) {
                throw new ValidationException(
                        "El proveedor " + seleccion.getProveedorId()
                                + " ya no es una opción vigente para el insumo " + seleccion.getInsumoId());
            }
        }
    }

    private void validarProveedoresSeleccionados(
            Set<Long> proveedorIds,
            Map<Long, ProveedorModel> proveedores) {
        for (Long proveedorId : proveedorIds) {
            ProveedorModel proveedor = proveedores.get(proveedorId);
            if (proveedor == null) {
                throw new ResourceNotFoundException("Proveedor no encontrado con id: " + proveedorId);
            }
            if (!Boolean.TRUE.equals(proveedor.getActivo())) {
                throw new ValidationException("El proveedor está inactivo: " + nombreProveedor(proveedor));
            }
        }
    }

    private Map<Long, Double> filasAMapa(List<Object[]> filas) {
        Map<Long, Double> resultado = new HashMap<>();
        for (Object[] fila : filas) {
            resultado.put(
                    ((Number) fila[0]).longValue(),
                    fila[1] == null ? 0.0 : ((Number) fila[1]).doubleValue());
        }
        return resultado;
    }

    private Comparator<ProveedorAbastecimientoDTO> comparadorProveedores() {
        return Comparator
                .comparing(ProveedorAbastecimientoDTO::getPuntaje, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(
                        ProveedorAbastecimientoDTO::getUltimaCompra,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ProveedorAbastecimientoDTO::getId);
    }

    private DetalleCompraModel detalleMasReciente(
            DetalleCompraModel primero,
            DetalleCompraModel segundo) {
        return comparadorDetallePorRecencia().compare(primero, segundo) >= 0 ? primero : segundo;
    }

    private Comparator<DetalleCompraModel> comparadorDetallePorRecencia() {
        return Comparator
                .comparing(
                        (DetalleCompraModel detalle) -> detalle.getCompra().getFechaCompra(),
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(DetalleCompraModel::getId, Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    private double calcularStockDisponibleReal(InsumoResponseDTO insumo) {
        if (insumo.getStockActual() != null || insumo.getStockApartado() != null) {
            return valor(insumo.getStockActual()) - valor(insumo.getStockApartado());
        }
        return valor(insumo.getStockDisponible());
    }

    private String construirExplicacion(
            String clasificacionAbc,
            double stockDisponible,
            double stockMinimo,
            double consumoMensual,
            double mesesCobertura,
            double cantidadAbierta,
            String unidad,
            ProveedorAbastecimientoDTO proveedorSugerido) {
        String explicacion = String.format(
                Locale.ROOT,
                "ABC %s: disponible %.3f %s, mínimo %.3f y consumo mensual promedio %.3f. "
                        + "La meta cubre %.1f meses",
                clasificacionAbc,
                stockDisponible,
                unidad == null ? "" : unidad,
                stockMinimo,
                consumoMensual,
                mesesCobertura);
        if (cantidadAbierta > 0) {
            explicacion += String.format(
                    Locale.ROOT,
                    "; se descontaron %.3f en compras abiertas",
                    cantidadAbierta);
        }
        if (proveedorSugerido == null) {
            return explicacion + ". No hay un proveedor compatible en el historial; debe seleccionarse manualmente.";
        }
        if (proveedorSugerido.getNumeroCompras() == null || proveedorSugerido.getNumeroCompras() == 0) {
            return explicacion + ". Se recomienda " + proveedorSugerido.getNombre()
                    + " por compatibilidad con el tipo de insumo y su calificación disponible.";
        }
        return explicacion + ". Se recomienda " + proveedorSugerido.getNombre()
                + " por historial de compra y calificación disponible.";
    }

    private String calcularPrioridad(String clasificacionAbc, double stockDisponible, double stockMinimo) {
        if (stockDisponible <= 0 || ("A".equals(clasificacionAbc) && stockDisponible <= stockMinimo)) {
            return "ALTA";
        }
        if (stockDisponible <= stockMinimo || "A".equals(clasificacionAbc) || "B".equals(clasificacionAbc)) {
            return "MEDIA";
        }
        return "BAJA";
    }

    private int ordenPrioridad(String prioridad) {
        return switch (prioridad) {
            case "ALTA" -> 0;
            case "MEDIA" -> 1;
            default -> 2;
        };
    }

    private double mesesCobertura(String clasificacionAbc) {
        return switch (clasificacionAbc) {
            case "A" -> 2.0;
            case "B" -> 1.5;
            default -> 1.0;
        };
    }

    private String normalizarClasificacion(String clasificacion) {
        if (clasificacion == null) {
            return "C";
        }
        String normalizada = clasificacion.trim().toUpperCase(Locale.ROOT);
        return Set.of("A", "B", "C").contains(normalizada) ? normalizada : "C";
    }

    private double puntajeRecencia(LocalDate fecha) {
        if (fecha == null) {
            return 0;
        }
        long dias = ChronoUnit.DAYS.between(fecha, LocalDate.now());
        if (dias <= 90) {
            return 10;
        }
        return dias <= 365 ? 5 : 0;
    }

    private String generarFolioBorrador(Long proveedorId) {
        return "BOR-" + LocalDate.now().toString().replace("-", "")
                + "-P" + proveedorId + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private String nombreProveedor(ProveedorModel proveedor) {
        if (proveedor.getRazonSocial() != null && !proveedor.getRazonSocial().isBlank()) {
            return proveedor.getRazonSocial().trim();
        }
        return Stream.of(
                        proveedor.getNombre(),
                        proveedor.getApellidoPaterno(),
                        proveedor.getApellidoMaterno())
                .filter(valor -> valor != null && !valor.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    private double valor(Double valor) {
        return valor == null ? 0.0 : valor;
    }

    private double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private double redondear(double valor, int escala) {
        return BigDecimal.valueOf(valor).setScale(escala, RoundingMode.HALF_UP).doubleValue();
    }

    private record ProveedorInsumoKey(Long insumoId, Long proveedorId) {
    }
}
