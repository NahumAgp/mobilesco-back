package com.mobilesco.mobilesco_back.modules.pedidoventa.application.usecases;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.*;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.*;
import com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.out.persistence.repositories.PedidoVentaRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.productoterminado.application.usecases.InventarioProductoTerminadoService;

@ExtendWith(MockitoExtension.class)
class PedidoVentaServiceTest {
    @Mock PedidoVentaRepository pedidoRepository;
    @Mock CotizacionRepository cotizacionRepository;
    @Mock InventarioProductoTerminadoService inventarioService;
    @Mock AlertaFaltanteInsumosPedidoService alertaFaltantesService;
    @InjectMocks PedidoVentaService service;

    @BeforeEach
    void guardarRegresaEntidad() {
        lenient().when(pedidoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void convierteCotizacionAceptadaConSnapshotsYFolio() {
        var cliente = ClienteModel.builder().id(2L).nombreComercial("Cliente Norte").build();
        var producto = ProductoModel.builder().id(3L).sku("SILLA-01").nombre("Silla").build();
        var cotizacion = CotizacionModel.builder().id(7L).folio("COT-7").estado(EstadoCotizacion.ACEPTADA)
                .cliente(cliente).total(new BigDecimal("2320.00")).detalles(new java.util.ArrayList<>()).build();
        cotizacion.agregarDetalle(CotizacionDetalleModel.builder().producto(producto).skuSnapshot("SILLA-01")
                .nombreSnapshot("Silla").cantidad(2).precioUnitario(new BigDecimal("1000.00"))
                .importe(new BigDecimal("2000.00")).build());
        when(cotizacionRepository.findById(7L)).thenReturn(Optional.of(cotizacion));
        doAnswer(invocation -> { PedidoVentaModel pedido = invocation.getArgument(0); pedido.setId(15L); return pedido; })
                .when(pedidoRepository).saveAndFlush(any());

        var respuesta = service.convertirCotizacion(7L, null, "ventas@mobilesco.mx");

        assertThat(respuesta.getFolio()).endsWith("-00015");
        assertThat(respuesta.getEstado()).isEqualTo("BORRADOR");
        assertThat(respuesta.getDetalles()).singleElement().satisfies(detalle -> {
            assertThat(detalle.getSku()).isEqualTo("SILLA-01");
            assertThat(detalle.getCantidadSolicitada()).isEqualByComparingTo("2.000");
            assertThat(detalle.getCantidadReservada()).isZero();
        });
        assertThat(cotizacion.getEstado()).isEqualTo(EstadoCotizacion.COMPLETADA);
        verify(cotizacionRepository).save(cotizacion);
    }

    @Test
    void confirmarReservaDisponibleAntesDeEvaluarElEstado() {
        var pedido = PedidoVentaModel.builder().id(9L).folio("PV-2026-00009").estado(EstadoPedidoVenta.BORRADOR)
                .total(BigDecimal.ZERO).detalles(new java.util.ArrayList<>()).build();
        var detalle = PedidoVentaDetalleModel.builder().pedido(pedido).producto(ProductoModel.builder().id(3L).build())
                .skuSnapshot("SILLA-01").nombreSnapshot("Silla").cantidadSolicitada(BigDecimal.ONE)
                .cantidadProducida(BigDecimal.ZERO).cantidadReservada(BigDecimal.ZERO).cantidadEntregada(BigDecimal.ZERO)
                .precioUnitario(BigDecimal.ZERO).importe(BigDecimal.ZERO).build();
        pedido.setDetalles(new java.util.ArrayList<>(List.of(detalle)));
        when(pedidoRepository.findById(9L)).thenReturn(Optional.of(pedido));
        doAnswer(invocation -> { detalle.setCantidadReservada(BigDecimal.ONE); return null; })
                .when(inventarioService).reservarDisponibles(pedido);

        var respuesta = service.confirmar(9L, "ventas@mobilesco.mx");

        assertThat(respuesta.getEstado()).isEqualTo("LISTO");
        verify(inventarioService).reservarDisponibles(pedido);
        verify(alertaFaltantesService).notificarFaltantes(9L);
    }
}
