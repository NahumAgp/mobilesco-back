package com.mobilesco.mobilesco_back.modules.tablero.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CuentaPorPagarRepository;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.out.persistence.repositories.RequisicionAlmacenRepository;
import com.mobilesco.mobilesco_back.modules.tablero.domain.PeriodoTablero;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories.DetalleSalidaInsumoRepository;

@ExtendWith(MockitoExtension.class)
class TableroServiceTest {
    @Mock CotizacionRepository cotizacionRepository;
    @Mock InsumoRepository insumoRepository;
    @Mock CompraRepository compraRepository;
    @Mock CuentaPorPagarRepository cuentaPorPagarRepository;
    @Mock RequisicionAlmacenRepository requisicionAlmacenRepository;
    @Mock ProductoRepository productoRepository;
    @Mock DetalleSalidaInsumoRepository detalleSalidaInsumoRepository;
    private TableroService service;

    @BeforeEach
    void setUp() {
        service = new TableroService(
                cotizacionRepository,
                insumoRepository,
                compraRepository,
                cuentaPorPagarRepository,
                requisicionAlmacenRepository,
                productoRepository,
                detalleSalidaInsumoRepository);
    }

    @Test
    void agregaIndicadoresRecientesYAlertasReales() {
        var resumen = mock(CotizacionRepository.ResumenPeriodoProjection.class);
        when(resumen.getCantidadActual()).thenReturn(8L);
        when(resumen.getMontoActual()).thenReturn(new BigDecimal("150000"));
        when(resumen.getMontoAnterior()).thenReturn(new BigDecimal("100000"));
        when(resumen.getCierresActuales()).thenReturn(3L);
        when(resumen.getDecisionesActuales()).thenReturn(4L);
        when(cotizacionRepository.resumirPeriodo(any(), any(), any())).thenReturn(resumen);
        when(cotizacionRepository.countByEstadoIn(any())).thenReturn(5L);
        when(cotizacionRepository.countByEstadoInAndFechaVencimientoBefore(any(), any())).thenReturn(2L);
        when(cotizacionRepository.countByEstadoInAndFechaVencimientoBetween(any(), any(), any())).thenReturn(1L);
        when(insumoRepository.countWithStockBajo()).thenReturn(3L);
        when(requisicionAlmacenRepository.countByEstadoIn(any())).thenReturn(4L);
        when(compraRepository.countByEstadoInAndActivoTrue(any())).thenReturn(2L);
        when(cuentaPorPagarRepository.countByEstadoInAndActivoTrue(any())).thenReturn(6L);
        when(cuentaPorPagarRepository.sumarSaldoPendientePorEstados(any())).thenReturn(12345.67);
        when(productoRepository.countByActivoTrue()).thenReturn(28L);
        when(insumoRepository.calcularValorTotalInventario()).thenReturn(100000.0);
        when(detalleSalidaInsumoRepository.sumarConsumoValorizado(any(), any())).thenReturn(25000.0);

        var reciente = mock(CotizacionRepository.CotizacionRecienteProjection.class);
        when(reciente.getId()).thenReturn(10L);
        when(reciente.getFolio()).thenReturn("COT-2026-00010");
        when(reciente.getClienteNombre()).thenReturn("Colegio Centro");
        when(reciente.getEstado()).thenReturn(EstadoCotizacion.ENVIADA);
        when(reciente.getTotal()).thenReturn(new BigDecimal("25000"));
        when(reciente.getFechaRegistro()).thenReturn(LocalDateTime.now());
        when(reciente.getPartidas()).thenReturn(2L);
        when(reciente.getUnidades()).thenReturn(12L);
        when(cotizacionRepository.encontrarRecientes(any(Pageable.class))).thenReturn(List.of(reciente));

        var resultado = service.obtenerResumen(PeriodoTablero.ULTIMOS_30_DIAS);

        assertEquals(5, resultado.getIndicadores().getCotizacionesActivas());
        assertEquals(8, resultado.getIndicadores().getCotizacionesPeriodo());
        assertEquals(new BigDecimal("150000.00"), resultado.getIndicadores().getMontoCotizado());
        assertEquals(new BigDecimal("50.0"), resultado.getIndicadores().getVariacionMontoPorcentaje());
        assertEquals(new BigDecimal("75.0"), resultado.getIndicadores().getTasaCierrePorcentaje());
        assertEquals(3, resultado.getIndicadoresOperativos().getInsumosStockBajo());
        assertEquals(4, resultado.getIndicadoresOperativos().getRequisicionesPendientes());
        assertEquals(2, resultado.getIndicadoresOperativos().getComprasPendientesRecepcion());
        assertEquals(6, resultado.getIndicadoresOperativos().getCuentasPorPagarPendientes());
        assertEquals(new BigDecimal("12345.67"), resultado.getIndicadoresOperativos().getSaldoPorPagar());
        assertEquals(28, resultado.getIndicadoresOperativos().getProductosActivos());
        assertEquals(new BigDecimal("100000.00"), resultado.getIndicadoresInventario().getValorTotalInventario());
        assertEquals(new BigDecimal("25000.00"), resultado.getIndicadoresInventario().getConsumoMensual());
        assertEquals(new BigDecimal("0.25"), resultado.getIndicadoresInventario().getRotacionMensual());
        assertEquals(1, resultado.getCotizacionesRecientes().size());
        assertEquals(3, resultado.getAlertas().size());
        assertEquals(LocalDate.now().minusDays(29), resultado.getDesde());
        assertEquals(LocalDate.now(), resultado.getHasta());
        verify(cotizacionRepository).encontrarRecientes(any(Pageable.class));
    }

    @Test
    void devuelveVaciosYVariacionNulaCuandoNoHayDatosComparables() {
        var resumen = mock(CotizacionRepository.ResumenPeriodoProjection.class);
        when(resumen.getMontoActual()).thenReturn(BigDecimal.ZERO);
        when(resumen.getMontoAnterior()).thenReturn(BigDecimal.ZERO);
        when(cotizacionRepository.resumirPeriodo(any(), any(), any())).thenReturn(resumen);
        when(cotizacionRepository.encontrarRecientes(any(Pageable.class))).thenReturn(List.of());

        var resultado = service.obtenerResumen(null);

        assertEquals(PeriodoTablero.MES, resultado.getPeriodo());
        assertEquals(0, resultado.getIndicadores().getCotizacionesPeriodo());
        assertEquals(new BigDecimal("0.0"), resultado.getIndicadores().getTasaCierrePorcentaje());
        assertNull(resultado.getIndicadores().getVariacionMontoPorcentaje());
        assertEquals(List.of(), resultado.getCotizacionesRecientes());
        assertEquals(List.of(), resultado.getAlertas());
    }
}
