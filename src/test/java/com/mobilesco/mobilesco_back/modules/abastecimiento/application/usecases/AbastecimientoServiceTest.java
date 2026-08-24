package com.mobilesco.mobilesco_back.modules.abastecimiento.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.ComprasBorradorResponseDTO;
import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.CrearComprasBorradorRequestDTO;
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
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

class AbastecimientoServiceTest {

    private InsumoService insumoService;
    private InsumoRepository insumoRepository;
    private ProveedorRepository proveedorRepository;
    private CompraRepository compraRepository;
    private DetalleCompraRepository detalleCompraRepository;
    private KardexRepository kardexRepository;
    private AbastecimientoService service;

    @BeforeEach
    void setUp() {
        insumoService = mock(InsumoService.class);
        insumoRepository = mock(InsumoRepository.class);
        proveedorRepository = mock(ProveedorRepository.class);
        compraRepository = mock(CompraRepository.class);
        detalleCompraRepository = mock(DetalleCompraRepository.class);
        kardexRepository = mock(KardexRepository.class);
        service = new AbastecimientoService(
                insumoService,
                insumoRepository,
                proveedorRepository,
                compraRepository,
                detalleCompraRepository,
                kardexRepository);
    }

    @Test
    void sugiereCantidadConAbcConsumoStockAbiertoYProveedorHistorico() {
        UnidadMedidaModel pieza = unidad(1L, "Pieza", "pz");
        InsumoModel insumoEntidad = insumo(1L, "INS-1", "Tablero", pieza, 2.0, 5.0, 20.0);
        InsumoResponseDTO insumo = InsumoResponseDTO.builder()
                .id(1L)
                .codigo("INS-1")
                .nombre("Tablero")
                .tipoInsumo("CARPINTERIA")
                .unidadMedidaId(1L)
                .unidadMedidaSimbolo("pz")
                .stockActual(2.0)
                .stockApartado(0.0)
                .stockDisponible(2.0)
                .stockMinimo(5.0)
                .clasificacionAbc("A")
                .costoCotizacion(20.0)
                .build();
        ProveedorModel proveedor = proveedor(7L, "Maderas del Centro", new BigDecimal("90.00"));
        CompraModel compra = CompraModel.builder()
                .id(20L)
                .proveedor(proveedor)
                .fechaCompra(LocalDate.now().minusDays(10))
                .estado("RECIBIDA")
                .activo(true)
                .build();
        DetalleCompraModel detalle = DetalleCompraModel.builder()
                .id(30L)
                .compra(compra)
                .insumo(insumoEntidad)
                .unidadCompra(pieza)
                .cantidad(5.0)
                .factorConversion(1.0)
                .precioUnitario(18.0)
                .build();

        when(insumoService.listarActivos()).thenReturn(List.of(insumo));
        when(kardexRepository.consumoPorInsumosEnPeriodo(eq(List.of(1L)), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[] {1L, 30.0}));
        when(detalleCompraRepository.cantidadPendientePorInsumos(List.of(1L)))
                .thenReturn(List.<Object[]>of(new Object[] {1L, 2.0}));
        when(detalleCompraRepository.findHistorialAbastecimiento(List.of(1L)))
                .thenReturn(List.of(detalle));
        when(proveedorRepository.findByActivo(true)).thenReturn(List.of(proveedor));

        List<SugerenciaAbastecimientoDTO> resultado = service.obtenerSugerencias();

        assertEquals(1, resultado.size());
        SugerenciaAbastecimientoDTO sugerencia = resultado.get(0);
        assertEquals("A", sugerencia.getClasificacionAbc());
        assertEquals(10.0, sugerencia.getConsumoMensual());
        assertEquals(10.0, sugerencia.getPuntoReorden());
        assertEquals(16.0, sugerencia.getCantidadSugerida());
        assertEquals("ALTA", sugerencia.getPrioridad());
        assertEquals(7L, sugerencia.getProveedorSugerido().getId());
        assertEquals(66.0, sugerencia.getProveedorSugerido().getPuntaje());
        assertTrue(sugerencia.getExplicacion().contains("compras abiertas"));
    }

    @Test
    void noSugiereCuandoUnaCompraAbiertaYaCubreElObjetivo() {
        InsumoResponseDTO insumo = InsumoResponseDTO.builder()
                .id(1L)
                .codigo("INS-1")
                .nombre("Tablero")
                .unidadMedidaId(1L)
                .unidadMedidaSimbolo("pz")
                .stockActual(2.0)
                .stockApartado(0.0)
                .stockDisponible(2.0)
                .stockMinimo(5.0)
                .clasificacionAbc("A")
                .build();
        when(insumoService.listarActivos()).thenReturn(List.of(insumo));
        when(kardexRepository.consumoPorInsumosEnPeriodo(anyCollection(), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[] {1L, 30.0}));
        when(detalleCompraRepository.cantidadPendientePorInsumos(anyCollection()))
                .thenReturn(List.<Object[]>of(new Object[] {1L, 20.0}));
        when(detalleCompraRepository.findHistorialAbastecimiento(anyCollection())).thenReturn(List.of());
        when(proveedorRepository.findByActivo(true)).thenReturn(List.of());

        assertTrue(service.obtenerSugerencias().isEmpty());
    }

    @Test
    void stockDisponibleNegativoIncrementaLaCantidadSugerida() {
        InsumoResponseDTO insumo = InsumoResponseDTO.builder()
                .id(1L)
                .codigo("INS-1")
                .nombre("Insumo reservado")
                .unidadMedidaId(1L)
                .unidadMedidaSimbolo("pz")
                .stockActual(2.0)
                .stockApartado(5.0)
                .stockDisponible(0.0)
                .stockMinimo(1.0)
                .clasificacionAbc("C")
                .build();
        when(insumoService.listarActivos()).thenReturn(List.of(insumo));
        when(kardexRepository.consumoPorInsumosEnPeriodo(anyCollection(), any(), any()))
                .thenReturn(List.of());
        when(detalleCompraRepository.cantidadPendientePorInsumos(anyCollection()))
                .thenReturn(List.of());
        when(detalleCompraRepository.findHistorialAbastecimiento(anyCollection())).thenReturn(List.of());
        when(proveedorRepository.findByActivo(true)).thenReturn(List.of());

        SugerenciaAbastecimientoDTO sugerencia = service.obtenerSugerencias().get(0);

        assertEquals(-3.0, sugerencia.getStockDisponible());
        assertEquals(5.0, sugerencia.getCantidadSugerida());
        assertEquals("ALTA", sugerencia.getPrioridad());
    }

    @Test
    void creaUnBorradorPorProveedorSinModificarStock() {
        UnidadMedidaModel pieza = unidad(1L, "Pieza", "pz");
        UnidadMedidaModel caja = unidad(2L, "Caja", "cja");
        InsumoModel insumo1 = insumo(1L, "I-1", "Herraje", pieza, 0.0, 5.0, 3.0);
        InsumoModel insumo2 = insumo(2L, "I-2", "Tela", pieza, 2.0, 4.0, 5.0);
        InsumoModel insumo3 = insumo(3L, "I-3", "Espuma", pieza, 0.0, 2.0, 7.0);
        ProveedorModel proveedor1 = proveedor(10L, "Proveedor Uno", null);
        ProveedorModel proveedor2 = proveedor(11L, "Proveedor Dos", null);
        List<DetalleCompraModel> historial = List.of(
                historial(101L, 100L, proveedor1, insumo1, caja, 2.0, 10.0),
                historial(102L, 101L, proveedor1, insumo2, pieza, 1.0, 5.0),
                historial(103L, 102L, proveedor2, insumo3, pieza, 1.0, 7.0));
        CrearComprasBorradorRequestDTO request = new CrearComprasBorradorRequestDTO();
        request.setSugerencias(List.of(
                seleccion(1L, 10.0, 10L),
                seleccion(2L, 6.0, 10L),
                seleccion(3L, 4.0, 11L)));

        when(insumoRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(insumo1, insumo2, insumo3));
        when(insumoService.listarActivos()).thenReturn(List.of(
                respuesta(insumo1, "C"),
                respuesta(insumo2, "C"),
                respuesta(insumo3, "C")));
        when(kardexRepository.consumoPorInsumosEnPeriodo(anyCollection(), any(), any())).thenReturn(List.of());
        when(detalleCompraRepository.cantidadPendientePorInsumos(anyCollection())).thenReturn(List.of());
        when(proveedorRepository.findByActivo(true)).thenReturn(List.of(proveedor1, proveedor2));
        when(proveedorRepository.findAllById(anyCollection())).thenReturn(List.of(proveedor1, proveedor2));
        when(detalleCompraRepository.findHistorialAbastecimiento(anyCollection()))
                .thenReturn(historial);
        AtomicLong ids = new AtomicLong(200);
        List<CompraModel> comprasGuardadas = new ArrayList<>();
        when(compraRepository.save(any(CompraModel.class))).thenAnswer(invocacion -> {
            CompraModel compra = invocacion.getArgument(0);
            if (compra.getId() == null) {
                compra.setId(ids.getAndIncrement());
            }
            comprasGuardadas.add(compra);
            return compra;
        });
        List<DetalleCompraModel> detallesGuardados = new ArrayList<>();
        when(detalleCompraRepository.saveAll(any())).thenAnswer(invocacion -> {
            Iterable<DetalleCompraModel> detalles = invocacion.getArgument(0);
            detalles.forEach(detallesGuardados::add);
            return detallesGuardados;
        });

        ComprasBorradorResponseDTO resultado = service.crearComprasBorrador(request);

        assertEquals(2, resultado.getCantidadCompras());
        assertEquals(3, resultado.getCantidadPartidas());
        assertTrue(resultado.getCompras().stream().allMatch(compra -> "BORRADOR".equals(compra.getEstado())));
        assertEquals(3, detallesGuardados.size());
        DetalleCompraModel primerDetalle = detallesGuardados.stream()
                .filter(detalle -> detalle.getInsumo().getId().equals(1L))
                .findFirst()
                .orElseThrow();
        assertEquals(5.0, primerDetalle.getCantidad());
        assertEquals(2.0, primerDetalle.getFactorConversion());
        assertEquals(0.0, primerDetalle.getCantidadRecibida());
        assertEquals(0.0, insumo1.getStockActual());
        assertEquals(2.0, insumo2.getStockActual());
        assertEquals(0.0, insumo3.getStockActual());
        verify(insumoRepository, never()).save(any(InsumoModel.class));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> idsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(insumoRepository).findAllByIdForUpdate(idsCaptor.capture());
        assertEquals(List.of(1L, 2L, 3L), new ArrayList<>(idsCaptor.getValue()));
    }

    @Test
    void mezclaProveedorHistoricoYCompatibleSinDuplicados() {
        UnidadMedidaModel pieza = unidad(1L, "Pieza", "pz");
        InsumoModel insumoEntidad = insumo(1L, "I-1", "Madera", pieza, 0.0, 2.0, 12.0);
        InsumoResponseDTO insumo = respuesta(insumoEntidad, "C");
        insumo.setTipoInsumo("CARPINTERIA");
        ProveedorModel historico = proveedor(10L, "Histórico", new BigDecimal("80"));
        ProveedorModel compatible = proveedor(11L, "Compatible", new BigDecimal("100"));
        TipoInsumoModel tipo = new TipoInsumoModel();
        tipo.setCodigo("CARPINTERIA");
        historico.setTipoInsumo(tipo);
        compatible.setTipoInsumo(tipo);
        DetalleCompraModel detalle = historial(1L, 1L, historico, insumoEntidad, pieza, 1.0, 11.0);

        when(insumoService.listarActivos()).thenReturn(List.of(insumo));
        when(kardexRepository.consumoPorInsumosEnPeriodo(anyCollection(), any(), any())).thenReturn(List.of());
        when(detalleCompraRepository.cantidadPendientePorInsumos(anyCollection())).thenReturn(List.of());
        when(detalleCompraRepository.findHistorialAbastecimiento(anyCollection())).thenReturn(List.of(detalle));
        when(proveedorRepository.findByActivo(true)).thenReturn(List.of(historico, compatible));

        SugerenciaAbastecimientoDTO sugerencia = service.obtenerSugerencias().get(0);

        assertEquals(List.of(10L, 11L), sugerencia.getProveedores().stream().map(p -> p.getId()).toList());
        assertEquals(1, sugerencia.getProveedores().stream().filter(p -> p.getId().equals(10L)).count());
        assertEquals(1, sugerencia.getProveedores().get(0).getNumeroCompras());
        assertEquals(11.0, sugerencia.getProveedores().get(0).getCostoUnitario());
        assertEquals(0, sugerencia.getProveedores().get(1).getNumeroCompras());
    }

    @Test
    void rechazaSeleccionQueDejoDeSerNecesariaDespuesDelLock() {
        UnidadMedidaModel pieza = unidad(1L, "Pieza", "pz");
        InsumoModel insumo = insumo(1L, "I-1", "Herraje", pieza, 0.0, 2.0, 5.0);
        ProveedorModel proveedor = proveedor(10L, "Proveedor", null);
        DetalleCompraModel historial = historial(1L, 1L, proveedor, insumo, pieza, 1.0, 5.0);
        prepararRevalidacion(insumo, proveedor, historial);
        when(detalleCompraRepository.cantidadPendientePorInsumos(anyCollection()))
                .thenReturn(List.<Object[]>of(new Object[] {1L, 4.0}));
        CrearComprasBorradorRequestDTO request = request(seleccion(1L, 4.0, 10L));

        assertThrows(ValidationException.class, () -> service.crearComprasBorrador(request));

        verify(insumoRepository).findAllByIdForUpdate(List.of(1L));
        verify(compraRepository, never()).save(any(CompraModel.class));
    }

    @Test
    void rechazaCantidadMayorALaSugerenciaRecalculada() {
        UnidadMedidaModel pieza = unidad(1L, "Pieza", "pz");
        InsumoModel insumo = insumo(1L, "I-1", "Herraje", pieza, 0.0, 2.0, 5.0);
        ProveedorModel proveedor = proveedor(10L, "Proveedor", null);
        prepararRevalidacion(
                insumo,
                proveedor,
                historial(1L, 1L, proveedor, insumo, pieza, 1.0, 5.0));
        when(detalleCompraRepository.cantidadPendientePorInsumos(anyCollection())).thenReturn(List.of());
        CrearComprasBorradorRequestDTO request = request(seleccion(1L, 4.1, 10L));

        assertThrows(ValidationException.class, () -> service.crearComprasBorrador(request));

        verify(proveedorRepository, never()).findAllById(anyCollection());
        verify(compraRepository, never()).save(any(CompraModel.class));
    }

    @Test
    void rechazaProveedorActivoQueNoEsOpcionActualDelInsumo() {
        UnidadMedidaModel pieza = unidad(1L, "Pieza", "pz");
        InsumoModel insumo = insumo(1L, "I-1", "Herraje", pieza, 0.0, 2.0, 5.0);
        ProveedorModel vigente = proveedor(10L, "Proveedor vigente", null);
        ProveedorModel ajeno = proveedor(11L, "Proveedor ajeno", null);
        prepararRevalidacion(
                insumo,
                vigente,
                historial(1L, 1L, vigente, insumo, pieza, 1.0, 5.0));
        when(proveedorRepository.findByActivo(true)).thenReturn(List.of(vigente, ajeno));
        when(detalleCompraRepository.cantidadPendientePorInsumos(anyCollection())).thenReturn(List.of());
        CrearComprasBorradorRequestDTO request = request(seleccion(1L, 4.0, 11L));

        assertThrows(ValidationException.class, () -> service.crearComprasBorrador(request));

        verify(proveedorRepository, never()).findAllById(anyCollection());
        verify(compraRepository, never()).save(any(CompraModel.class));
    }

    @Test
    void segundoEnvioRecalculaYNoDuplicaLaCompraDelPrimerEnvio() {
        UnidadMedidaModel pieza = unidad(1L, "Pieza", "pz");
        InsumoModel insumo = insumo(1L, "I-1", "Herraje", pieza, 0.0, 2.0, 5.0);
        ProveedorModel proveedor = proveedor(10L, "Proveedor", null);
        prepararRevalidacion(
                insumo,
                proveedor,
                historial(1L, 1L, proveedor, insumo, pieza, 1.0, 5.0));
        when(detalleCompraRepository.cantidadPendientePorInsumos(anyCollection()))
                .thenReturn(List.of())
                .thenReturn(List.<Object[]>of(new Object[] {1L, 4.0}));
        when(proveedorRepository.findAllById(anyCollection())).thenReturn(List.of(proveedor));
        AtomicLong compraId = new AtomicLong(100L);
        when(compraRepository.save(any(CompraModel.class))).thenAnswer(invocacion -> {
            CompraModel compra = invocacion.getArgument(0);
            if (compra.getId() == null) {
                compra.setId(compraId.getAndIncrement());
            }
            return compra;
        });
        when(detalleCompraRepository.saveAll(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        CrearComprasBorradorRequestDTO request = request(seleccion(1L, 4.0, 10L));

        ComprasBorradorResponseDTO primera = service.crearComprasBorrador(request);
        assertThrows(ValidationException.class, () -> service.crearComprasBorrador(request));

        assertEquals(1, primera.getCantidadCompras());
        verify(insumoRepository, times(2)).findAllByIdForUpdate(List.of(1L));
        verify(compraRepository, times(2)).save(any(CompraModel.class));
        verify(detalleCompraRepository, times(1)).saveAll(any());
    }

    @Test
    void rechazaSeleccionDuplicadaAntesDeCrearCompras() {
        CrearComprasBorradorRequestDTO request = new CrearComprasBorradorRequestDTO();
        request.setSugerencias(List.of(
                seleccion(1L, 2.0, 10L),
                seleccion(1L, 3.0, 11L)));

        assertThrows(ValidationException.class, () -> service.crearComprasBorrador(request));
        verify(compraRepository, never()).save(any(CompraModel.class));
    }

    @Test
    void rechazaCantidadNoFinitaAntesDeCrearCompras() {
        CrearComprasBorradorRequestDTO request = new CrearComprasBorradorRequestDTO();
        request.setSugerencias(List.of(seleccion(1L, Double.POSITIVE_INFINITY, 10L)));

        assertThrows(ValidationException.class, () -> service.crearComprasBorrador(request));
        verify(compraRepository, never()).save(any(CompraModel.class));
    }

    private UnidadMedidaModel unidad(Long id, String nombre, String simbolo) {
        UnidadMedidaModel unidad = new UnidadMedidaModel();
        unidad.setId(id);
        unidad.setNombre(nombre);
        unidad.setSimbolo(simbolo);
        unidad.setEstado(true);
        return unidad;
    }

    private InsumoModel insumo(
            Long id,
            String codigo,
            String nombre,
            UnidadMedidaModel unidad,
            Double stock,
            Double minimo,
            Double costo) {
        return InsumoModel.builder()
                .id(id)
                .codigo(codigo)
                .nombre(nombre)
                .tipoInsumo("CARPINTERIA")
                .unidadMedida(unidad)
                .stockActual(stock)
                .stockApartado(0.0)
                .stockMinimo(minimo)
                .costoCotizacion(costo)
                .activo(true)
                .build();
    }

    private ProveedorModel proveedor(Long id, String razonSocial, BigDecimal calificacion) {
        ProveedorModel proveedor = new ProveedorModel();
        proveedor.setId(id);
        proveedor.setRazonSocial(razonSocial);
        proveedor.setCalificacionProveedor(calificacion);
        proveedor.setActivo(true);
        return proveedor;
    }

    private SeleccionSugerenciaDTO seleccion(Long insumoId, Double cantidad, Long proveedorId) {
        SeleccionSugerenciaDTO seleccion = new SeleccionSugerenciaDTO();
        seleccion.setInsumoId(insumoId);
        seleccion.setCantidad(cantidad);
        seleccion.setProveedorId(proveedorId);
        return seleccion;
    }

    private CrearComprasBorradorRequestDTO request(SeleccionSugerenciaDTO... selecciones) {
        CrearComprasBorradorRequestDTO request = new CrearComprasBorradorRequestDTO();
        request.setSugerencias(List.of(selecciones));
        return request;
    }

    private InsumoResponseDTO respuesta(InsumoModel insumo, String clasificacionAbc) {
        return InsumoResponseDTO.builder()
                .id(insumo.getId())
                .codigo(insumo.getCodigo())
                .nombre(insumo.getNombre())
                .tipoInsumo(insumo.getTipoInsumo())
                .unidadMedidaId(insumo.getUnidadMedida().getId())
                .unidadMedidaNombre(insumo.getUnidadMedida().getNombre())
                .unidadMedidaSimbolo(insumo.getUnidadMedida().getSimbolo())
                .stockActual(insumo.getStockActual())
                .stockApartado(insumo.getStockApartado())
                .stockDisponible(insumo.getStockActual() - insumo.getStockApartado())
                .stockMinimo(insumo.getStockMinimo())
                .costoCotizacion(insumo.getCostoCotizacion())
                .clasificacionAbc(clasificacionAbc)
                .activo(true)
                .build();
    }

    private DetalleCompraModel historial(
            Long detalleId,
            Long compraId,
            ProveedorModel proveedor,
            InsumoModel insumo,
            UnidadMedidaModel unidadCompra,
            double factorConversion,
            double precioUnitario) {
        CompraModel compra = CompraModel.builder()
                .id(compraId)
                .proveedor(proveedor)
                .fechaCompra(LocalDate.now().minusDays(10))
                .estado("RECIBIDA")
                .activo(true)
                .build();
        return DetalleCompraModel.builder()
                .id(detalleId)
                .compra(compra)
                .insumo(insumo)
                .unidadCompra(unidadCompra)
                .cantidad(1.0)
                .cantidadRecibida(1.0)
                .factorConversion(factorConversion)
                .precioUnitario(precioUnitario)
                .subtotal(precioUnitario)
                .build();
    }

    private void prepararRevalidacion(
            InsumoModel insumo,
            ProveedorModel proveedor,
            DetalleCompraModel historial) {
        when(insumoRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(insumo));
        when(insumoService.listarActivos()).thenReturn(List.of(respuesta(insumo, "C")));
        when(kardexRepository.consumoPorInsumosEnPeriodo(anyCollection(), any(), any())).thenReturn(List.of());
        when(detalleCompraRepository.findHistorialAbastecimiento(anyCollection()))
                .thenReturn(List.of(historial));
        when(proveedorRepository.findByActivo(true)).thenReturn(List.of(proveedor));
    }
}
