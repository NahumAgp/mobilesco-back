package com.mobilesco.mobilesco_back.modules.cotizacion.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.out.persistence.repositories.ClienteRepository;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.CotizacionModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos.CotizacionRequestDTO;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.application.usecases.ProductoService;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.*;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {
    @Mock CotizacionRepository cotizacionRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock ProductoRepository productoRepository;
    @Mock ProductoService productoService;
    CotizacionService service;

    @BeforeEach
    void setUp() {
        service = new CotizacionService(cotizacionRepository, clienteRepository, productoRepository, productoService);
    }

    @Test
    void creaCotizacionRecalculandoImportesEnServidor() {
        ClienteModel cliente = ClienteModel.builder().id(7L).nombre("Colegio Centro").activo(true).build();
        ProductoModel producto = ProductoModel.builder().id(9L).sku("ESC-001").nombre("Pupitre").activo(true).build();
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(9L)).thenReturn(Optional.of(producto));
        when(productoService.obtenerEstructuraCostos(9L)).thenReturn(costosCompletos());
        doAnswer(inv -> {
            CotizacionModel c = inv.getArgument(0);
            c.setId(12L);
            return c;
        }).when(cotizacionRepository).saveAndFlush(any(CotizacionModel.class));
        when(cotizacionRepository.save(any(CotizacionModel.class))).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = service.crear(solicitud());

        assertEquals("COT-" + java.time.LocalDate.now().getYear() + "-00012", respuesta.getFolio());
        assertEquals(new BigDecimal("307.70"), respuesta.getSubtotalVenta());
        assertEquals(new BigDecimal("356.93"), respuesta.getTotal());
        assertEquals(1, respuesta.getDetalles().size());
        verify(cotizacionRepository).saveAndFlush(any(CotizacionModel.class));
    }

    @Test
    void rechazaProductoSinEsquemaCompletoDeCostos() {
        ClienteModel cliente = ClienteModel.builder().id(7L).nombre("Colegio Centro").activo(true).build();
        ProductoModel producto = ProductoModel.builder().id(9L).sku("ESC-001").nombre("Pupitre").activo(true).build();
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(9L)).thenReturn(Optional.of(producto));
        when(productoService.obtenerEstructuraCostos(9L)).thenReturn(ProductoEstructuraCostosDTO.builder()
                .costoTotal(0.0).insumos(List.of()).operaciones(List.of()).build());

        ValidationException error = assertThrows(ValidationException.class, () -> service.crear(solicitud()));

        assertTrue(error.getMessage().contains("insumos"));
        assertTrue(error.getMessage().contains("operaciones"));
        verify(cotizacionRepository, never()).save(any());
    }

    @Test
    void permiteCrearCotizacionSinClienteComoPublicoGeneral() {
        ProductoModel producto = ProductoModel.builder().id(9L).sku("ESC-001").nombre("Pupitre").activo(true).build();
        when(productoRepository.findById(9L)).thenReturn(Optional.of(producto));
        when(productoService.obtenerEstructuraCostos(9L)).thenReturn(costosCompletos());
        doAnswer(inv -> {
            CotizacionModel c = inv.getArgument(0);
            c.setId(13L);
            return c;
        }).when(cotizacionRepository).saveAndFlush(any(CotizacionModel.class));
        when(cotizacionRepository.save(any(CotizacionModel.class))).thenAnswer(inv -> inv.getArgument(0));
        CotizacionRequestDTO dto = solicitud();
        dto.setClienteId(null);

        var respuesta = service.crear(dto);

        assertNull(respuesta.getClienteId());
        assertEquals("Público general", respuesta.getClienteNombre());
        verify(clienteRepository, never()).findById(any());
    }

    @Test
    void buscarProductosMantieneLosIncompletosComoNoCotizables() {
        ProductoModel producto = ProductoModel.builder()
                .id(9L)
                .sku("ESC-001")
                .nombre("Pupitre")
                .activo(true)
                .build();
        when(productoRepository.buscarPaginado(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(producto)));
        when(productoService.obtenerEstructuraCostos(9L))
                .thenThrow(new ValidationException("No cotizable: falta operaciones"));

        var resultados = service.buscarProductos("Pupitre", null);

        assertEquals(1, resultados.size());
        assertFalse(resultados.get(0).isCotizable());
        assertTrue(resultados.get(0).getFaltantes().get(0).contains("operaciones"));
    }

    private CotizacionRequestDTO solicitud() {
        CotizacionRequestDTO dto = new CotizacionRequestDTO();
        dto.setClienteId(7L);
        dto.setMargenPorcentaje(new BigDecimal("35"));
        dto.setDescuentoPorcentaje(BigDecimal.ZERO);
        dto.setFlete(BigDecimal.ZERO);
        dto.setIvaPorcentaje(new BigDecimal("16"));
        CotizacionRequestDTO.DetalleRequest detalle = new CotizacionRequestDTO.DetalleRequest();
        detalle.setProductoId(9L);
        detalle.setCantidad(2);
        dto.setDetalles(List.of(detalle));
        return dto;
    }

    private ProductoEstructuraCostosDTO costosCompletos() {
        return ProductoEstructuraCostosDTO.builder()
                .costoTotal(100.0)
                .costoCif(10.0)
                .tasaCifMinuto(2.0)
                .configuracionCifId(1L)
                .insumos(List.of(ProductoInsumoResponseDTO.builder()
                        .cantidad(2.0).costoUnitario(20.0).build()))
                .operaciones(List.of(ProductoOperacionResponseDTO.builder()
                        .cantidad(1).tiempoTotal(5.0).costoMinutoOperacion(10.0).importeActividad(50.0).build()))
                .build();
    }
}
