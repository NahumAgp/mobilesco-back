package com.mobilesco.mobilesco_back.modules.compra.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.DetalleCompraModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraCreateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories.UnidadMedidaRepository;

class DetalleCompraServiceBorradorTest {

    private DetalleCompraRepository detalleRepository;
    private CompraRepository compraRepository;
    private InsumoRepository insumoRepository;
    private UnidadMedidaRepository unidadMedidaRepository;
    private KardexService kardexService;
    private DetalleCompraService service;

    @BeforeEach
    void setUp() {
        detalleRepository = mock(DetalleCompraRepository.class);
        compraRepository = mock(CompraRepository.class);
        insumoRepository = mock(InsumoRepository.class);
        unidadMedidaRepository = mock(UnidadMedidaRepository.class);
        kardexService = mock(KardexService.class);
        service = new DetalleCompraService(
                detalleRepository,
                compraRepository,
                insumoRepository,
                unidadMedidaRepository,
                kardexService);
    }

    @Test
    void reemplazaDosDetallesPorUnoConSubtotalServidorCantidadRecibidaCeroYLockOrdenado() {
        CompraModel borrador = borrador(10L);
        UnidadMedidaModel pieza = unidad(20L, "Pieza", "pz");
        InsumoModel anteriorMayor = insumo(5L, "Anterior mayor", pieza);
        InsumoModel anteriorMenor = insumo(2L, "Anterior menor", pieza);
        InsumoModel nuevo = insumo(3L, "Nuevo", pieza);
        List<DetalleCompraModel> anteriores = List.of(
                detalle(51L, borrador, anteriorMayor, pieza),
                detalle(52L, borrador, anteriorMenor, pieza));
        DetalleCompraCreateDTO dto = detalleDto(3L, 20L, 2.5, 4.0, 7.0);
        dto.setCantidadRecibida(2.0);
        dto.setSubtotal(999.0);

        when(compraRepository.findById(10L)).thenReturn(Optional.of(borrador));
        when(detalleRepository.findByCompraId(10L)).thenReturn(anteriores);
        when(insumoRepository.findAllByIdForUpdate(List.of(2L, 3L, 5L)))
                .thenReturn(List.of(anteriorMenor, nuevo, anteriorMayor));
        when(unidadMedidaRepository.findById(20L)).thenReturn(Optional.of(pieza));

        double subtotal = service.reemplazarDetallesEditables(10L, List.of(dto));

        assertEquals(10.0, subtotal);
        verify(insumoRepository).findAllByIdForUpdate(List.of(2L, 3L, 5L));
        verify(detalleRepository).deleteAll(anteriores);
        verify(detalleRepository).flush();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DetalleCompraModel>> captor = ArgumentCaptor.forClass(List.class);
        verify(detalleRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        DetalleCompraModel guardado = captor.getValue().get(0);
        assertSame(nuevo, guardado.getInsumo());
        assertSame(pieza, guardado.getUnidadCompra());
        assertEquals(2.5, guardado.getCantidad());
        assertEquals(7.0, guardado.getFactorConversion());
        assertEquals(4.0, guardado.getPrecioUnitario());
        assertEquals(10.0, guardado.getSubtotal());
        assertEquals(0.0, guardado.getCantidadRecibida());

        InOrder orden = inOrder(insumoRepository, unidadMedidaRepository, detalleRepository);
        orden.verify(insumoRepository).findAllByIdForUpdate(List.of(2L, 3L, 5L));
        orden.verify(unidadMedidaRepository).findById(20L);
        orden.verify(detalleRepository).deleteAll(anteriores);
        orden.verify(detalleRepository).flush();
        orden.verify(detalleRepository).saveAll(any());
    }

    @Test
    void dtoInvalidoNoBorraLosDetallesAnteriores() {
        when(compraRepository.findById(10L)).thenReturn(Optional.of(borrador(10L)));
        DetalleCompraCreateDTO invalido = detalleDto(3L, 20L, 0.0, 4.0, 1.0);

        assertThrows(
                ValidationException.class,
                () -> service.reemplazarDetallesEditables(10L, List.of(invalido)));

        verify(detalleRepository, never()).findByCompraId(any());
        verify(detalleRepository, never()).deleteAll(any());
        verify(detalleRepository, never()).flush();
        verify(detalleRepository, never()).saveAll(any());
        verify(insumoRepository, never()).findAllByIdForUpdate(any());
    }

    @Test
    void overflowAInfinityNoBorraLosDetallesAnteriores() {
        CompraModel borrador = borrador(10L);
        UnidadMedidaModel pieza = unidad(20L, "Pieza", "pz");
        InsumoModel anterior = insumo(2L, "Anterior", pieza);
        InsumoModel nuevo = insumo(3L, "Nuevo", pieza);
        List<DetalleCompraModel> anteriores = List.of(detalle(51L, borrador, anterior, pieza));
        DetalleCompraCreateDTO overflow = detalleDto(
                3L,
                20L,
                Double.MAX_VALUE,
                2.0,
                1.0);

        when(compraRepository.findById(10L)).thenReturn(Optional.of(borrador));
        when(detalleRepository.findByCompraId(10L)).thenReturn(anteriores);
        when(insumoRepository.findAllByIdForUpdate(List.of(2L, 3L)))
                .thenReturn(List.of(anterior, nuevo));
        when(unidadMedidaRepository.findById(20L)).thenReturn(Optional.of(pieza));

        assertThrows(
                ValidationException.class,
                () -> service.reemplazarDetallesEditables(10L, List.of(overflow)));

        verify(insumoRepository).findAllByIdForUpdate(List.of(2L, 3L));
        verify(detalleRepository, never()).deleteAll(any());
        verify(detalleRepository, never()).flush();
        verify(detalleRepository, never()).saveAll(any());
    }

    @Test
    void pendienteSinRecepcionesPermiteReemplazarDetalles() {
        CompraModel pendiente = borrador(10L);
        pendiente.setEstado("PENDIENTE");
        UnidadMedidaModel pieza = unidad(20L, "Pieza", "pz");
        InsumoModel anterior = insumo(2L, "Anterior", pieza);
        InsumoModel nuevo = insumo(3L, "Nuevo", pieza);
        List<DetalleCompraModel> anteriores = List.of(detalle(51L, pendiente, anterior, pieza));
        DetalleCompraCreateDTO dto = detalleDto(3L, 20L, 3.0, 20.0, 1.0);

        when(compraRepository.findById(10L)).thenReturn(Optional.of(pendiente));
        when(detalleRepository.findByCompraId(10L)).thenReturn(anteriores);
        when(insumoRepository.findAllByIdForUpdate(List.of(2L, 3L))).thenReturn(List.of(anterior, nuevo));
        when(unidadMedidaRepository.findById(20L)).thenReturn(Optional.of(pieza));

        double subtotal = service.reemplazarDetallesEditables(10L, List.of(dto));

        assertEquals(60.0, subtotal);
        verify(detalleRepository).deleteAll(anteriores);
        verify(detalleRepository).saveAll(any());
    }

    @Test
    void pendienteConRecepcionNoBorraDetalles() {
        CompraModel pendiente = borrador(10L);
        pendiente.setEstado("PENDIENTE");
        UnidadMedidaModel pieza = unidad(20L, "Pieza", "pz");
        InsumoModel anterior = insumo(2L, "Anterior", pieza);
        DetalleCompraModel recibido = detalle(51L, pendiente, anterior, pieza);
        recibido.setCantidadRecibida(1.0);

        when(compraRepository.findById(10L)).thenReturn(Optional.of(pendiente));
        when(detalleRepository.findByCompraId(10L)).thenReturn(List.of(recibido));

        assertThrows(
                ValidationException.class,
                () -> service.reemplazarDetallesEditables(
                        10L,
                        List.of(detalleDto(3L, 20L, 3.0, 20.0, 1.0))));

        verify(detalleRepository, never()).deleteAll(any());
        verify(detalleRepository, never()).flush();
        verify(detalleRepository, never()).saveAll(any());
    }

    @Test
    void borradorTampocoPermiteRecepcionParcial() {
        CompraModel borrador = borrador(10L);
        DetalleCompraModel detalle = DetalleCompraModel.builder()
                .id(20L)
                .compra(borrador)
                .cantidad(5.0)
                .cantidadRecibida(0.0)
                .factorConversion(1.0)
                .build();
        when(detalleRepository.findById(20L)).thenReturn(Optional.of(detalle));

        assertThrows(
                ValidationException.class,
                () -> service.recibirParcial(20L, 1.0, "Operador", null));

        verify(insumoRepository, never()).save(any(InsumoModel.class));
        verify(detalleRepository, never()).save(any(DetalleCompraModel.class));
        verify(kardexService, never()).registrarEntradaCompra(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    private CompraModel borrador(Long id) {
        return CompraModel.builder()
                .id(id)
                .estado("BORRADOR")
                .activo(true)
                .build();
    }

    private InsumoModel insumo(Long id, String nombre, UnidadMedidaModel unidad) {
        return InsumoModel.builder()
                .id(id)
                .nombre(nombre)
                .unidadMedida(unidad)
                .activo(true)
                .build();
    }

    private UnidadMedidaModel unidad(Long id, String nombre, String simbolo) {
        UnidadMedidaModel unidad = new UnidadMedidaModel();
        unidad.setId(id);
        unidad.setNombre(nombre);
        unidad.setSimbolo(simbolo);
        unidad.setEstado(true);
        return unidad;
    }

    private DetalleCompraModel detalle(
            Long id,
            CompraModel compra,
            InsumoModel insumo,
            UnidadMedidaModel unidad) {
        return DetalleCompraModel.builder()
                .id(id)
                .compra(compra)
                .insumo(insumo)
                .unidadCompra(unidad)
                .cantidad(1.0)
                .factorConversion(1.0)
                .precioUnitario(1.0)
                .cantidadRecibida(0.0)
                .subtotal(1.0)
                .build();
    }

    private DetalleCompraCreateDTO detalleDto(
            Long insumoId,
            Long unidadId,
            Double cantidad,
            Double precio,
            Double factor) {
        DetalleCompraCreateDTO dto = new DetalleCompraCreateDTO();
        dto.setInsumoId(insumoId);
        dto.setUnidadCompraId(unidadId);
        dto.setCantidad(cantidad);
        dto.setPrecioUnitario(precio);
        dto.setFactorConversion(factor);
        return dto;
    }
}
