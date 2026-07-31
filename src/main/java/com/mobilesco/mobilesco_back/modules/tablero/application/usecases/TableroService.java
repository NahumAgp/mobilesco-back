package com.mobilesco.mobilesco_back.modules.tablero.application.usecases;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CuentaPorPagarRepository;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.EstadoRequisicionAlmacen;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.out.persistence.repositories.RequisicionAlmacenRepository;
import com.mobilesco.mobilesco_back.modules.tablero.domain.PeriodoTablero;
import com.mobilesco.mobilesco_back.modules.tablero.infrastructure.in.api.dtos.TableroResumenDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableroService {
    private static final List<EstadoCotizacion> ACTIVAS = List.of(
            EstadoCotizacion.BORRADOR, EstadoCotizacion.PENDIENTE, EstadoCotizacion.ENVIADA);
    private static final List<String> COMPRAS_PENDIENTES = List.of("PENDIENTE", "RECIBIDA_PARCIAL");
    private static final List<String> CUENTAS_PENDIENTES = List.of("PENDIENTE", "PARCIAL");
    private static final List<EstadoRequisicionAlmacen> REQUISICIONES_PENDIENTES = List.of(
            EstadoRequisicionAlmacen.ENVIADA, EstadoRequisicionAlmacen.EN_REVISION);

    private final CotizacionRepository cotizacionRepository;
    private final InsumoRepository insumoRepository;
    private final CompraRepository compraRepository;
    private final CuentaPorPagarRepository cuentaPorPagarRepository;
    private final RequisicionAlmacenRepository requisicionAlmacenRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public TableroResumenDTO obtenerResumen(PeriodoTablero periodoSolicitado) {
        PeriodoTablero periodo = periodoSolicitado == null ? PeriodoTablero.MES : periodoSolicitado;
        LocalDate hoy = LocalDate.now();
        Rango rango = calcularRango(periodo, hoy);
        var resumen = cotizacionRepository.resumirPeriodo(
                rango.desdeAnterior().atStartOfDay(), rango.desde().atStartOfDay(), rango.hasta().atStartOfDay());

        long activas = cotizacionRepository.countByEstadoIn(ACTIVAS);
        long vencidas = cotizacionRepository.countByEstadoInAndFechaVencimientoBefore(ACTIVAS, hoy);
        long porVencer = cotizacionRepository.countByEstadoInAndFechaVencimientoBetween(ACTIVAS, hoy, hoy.plusDays(7));
        long stockBajo = insumoRepository.countWithStockBajo();
        long requisicionesPendientes = requisicionAlmacenRepository.countByEstadoIn(REQUISICIONES_PENDIENTES);
        long comprasPendientes = compraRepository.countByEstadoInAndActivoTrue(COMPRAS_PENDIENTES);
        long cuentasPendientes = cuentaPorPagarRepository.countByEstadoInAndActivoTrue(CUENTAS_PENDIENTES);
        Double saldoPendiente = cuentaPorPagarRepository.sumarSaldoPendientePorEstados(CUENTAS_PENDIENTES);
        BigDecimal saldoPorPagar = BigDecimal.valueOf(saldoPendiente == null ? 0 : saldoPendiente);
        long productosActivos = productoRepository.countByActivoTrue();

        return TableroResumenDTO.builder()
                .periodo(periodo).desde(rango.desde()).hasta(rango.hasta().minusDays(1))
                .indicadores(TableroResumenDTO.Indicadores.builder()
                        .cotizacionesActivas(activas)
                        .cotizacionesPeriodo(valor(resumen == null ? null : resumen.getCantidadActual()))
                        .montoCotizado(dinero(resumen == null ? null : resumen.getMontoActual()))
                        .variacionMontoPorcentaje(variacion(
                                resumen == null ? null : resumen.getMontoActual(),
                                resumen == null ? null : resumen.getMontoAnterior()))
                        .tasaCierrePorcentaje(tasa(
                                resumen == null ? null : resumen.getCierresActuales(),
                                resumen == null ? null : resumen.getDecisionesActuales()))
                        .build())
                .indicadoresOperativos(TableroResumenDTO.IndicadoresOperativos.builder()
                        .insumosStockBajo(stockBajo)
                        .requisicionesPendientes(requisicionesPendientes)
                        .comprasPendientesRecepcion(comprasPendientes)
                        .cuentasPorPagarPendientes(cuentasPendientes)
                        .saldoPorPagar(dinero(saldoPorPagar))
                        .productosActivos(productosActivos)
                        .build())
                .cotizacionesRecientes(cotizacionRepository.encontrarRecientes(PageRequest.of(0, 5)).stream()
                        .map(c -> TableroResumenDTO.CotizacionReciente.builder()
                                .id(c.getId()).folio(c.getFolio()).cliente(c.getClienteNombre())
                                .estado(c.getEstado()).total(dinero(c.getTotal())).fechaRegistro(c.getFechaRegistro())
                                .partidas(valor(c.getPartidas())).unidades(valor(c.getUnidades())).build())
                        .toList())
                .alertas(crearAlertas(vencidas, porVencer, stockBajo))
                .build();
    }

    private List<TableroResumenDTO.Alerta> crearAlertas(long vencidas, long porVencer, long stockBajo) {
        List<TableroResumenDTO.Alerta> alertas = new ArrayList<>();
        if (vencidas > 0) {
            alertas.add(alerta("COTIZACIONES_VENCIDAS", "danger", "Cotizaciones vencidas",
                    vencidas + " cotización(es) activa(s) superaron su fecha de vigencia.", vencidas,
                    "/cotizaciones"));
        }
        if (porVencer > 0) {
            alertas.add(alerta("COTIZACIONES_POR_VENCER", "warning", "Seguimientos próximos",
                    porVencer + " cotización(es) activa(s) vencen durante los próximos 7 días.", porVencer,
                    "/cotizaciones"));
        }
        if (stockBajo > 0) {
            alertas.add(alerta("STOCK_BAJO", "primary", "Insumos con stock bajo",
                    stockBajo + " insumo(s) alcanzaron o bajaron de su existencia mínima.", stockBajo,
                    "/insumos"));
        }
        return alertas;
    }

    private TableroResumenDTO.Alerta alerta(
            String tipo, String severidad, String titulo, String descripcion, long cantidad, String ruta) {
        return TableroResumenDTO.Alerta.builder().tipo(tipo).severidad(severidad).titulo(titulo)
                .descripcion(descripcion).cantidad(cantidad).ruta(ruta).build();
    }

    private Rango calcularRango(PeriodoTablero periodo, LocalDate hoy) {
        LocalDate hasta = hoy.plusDays(1);
        LocalDate desde = switch (periodo) {
            case MES -> hoy.withDayOfMonth(1);
            case ULTIMOS_30_DIAS -> hoy.minusDays(29);
            case TRIMESTRE -> hoy.minusDays(89);
            case ANIO -> hoy.withDayOfYear(1);
        };
        LocalDate desdeAnterior = desde.minusDays(ChronoUnit.DAYS.between(desde, hasta));
        return new Rango(desdeAnterior, desde, hasta);
    }

    private BigDecimal variacion(BigDecimal actual, BigDecimal anterior) {
        BigDecimal base = dinero(anterior);
        if (base.signum() == 0) return null;
        return dinero(actual).subtract(base).multiply(BigDecimal.valueOf(100))
                .divide(base, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal tasa(Long cierres, Long decisiones) {
        long total = valor(decisiones);
        if (total == 0) return BigDecimal.ZERO.setScale(1);
        return BigDecimal.valueOf(valor(cierres) * 100L)
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal dinero(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    private long valor(Number valor) {
        return valor == null ? 0 : valor.longValue();
    }

    private record Rango(LocalDate desdeAnterior, LocalDate desde, LocalDate hasta) {}
}
