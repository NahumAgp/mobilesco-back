package com.mobilesco.mobilesco_back.modules.compra.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.CuentaPorPagarModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.DetalleCompraModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CompraResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CompraUpdateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraCreateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CuentaPorPagarRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.out.persistence.repositories.ProveedorRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

class CompraServiceBorradorTest {

    private CompraRepository compraRepository;
    private ProveedorRepository proveedorRepository;
    private InsumoRepository insumoRepository;
    private DetalleCompraRepository detalleCompraRepository;
    private CuentaPorPagarRepository cuentaPorPagarRepository;
    private DetalleCompraService detalleCompraService;
    private KardexService kardexService;
    private CompraService service;

    @BeforeEach
    void setUp() {
        compraRepository = mock(CompraRepository.class);
        proveedorRepository = mock(ProveedorRepository.class);
        insumoRepository = mock(InsumoRepository.class);
        detalleCompraRepository = mock(DetalleCompraRepository.class);
        cuentaPorPagarRepository = mock(CuentaPorPagarRepository.class);
        detalleCompraService = mock(DetalleCompraService.class);
        kardexService = mock(KardexService.class);
        service = new CompraService(
                compraRepository,
                proveedorRepository,
                insumoRepository,
                detalleCompraRepository,
                cuentaPorPagarRepository,
                detalleCompraService,
                kardexService);
    }

    @Test
    void borradorNoCreaCuentaPorPagarAunqueSeaCredito() {
        CompraModel borrador = borradorCredito();
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(borrador));
        when(compraRepository.save(borrador)).thenReturn(borrador);
        when(detalleCompraService.recalcularSubtotalEditable(50L)).thenReturn(100.0);
        when(cuentaPorPagarRepository.findByCompraId(50L)).thenReturn(Optional.empty());
        when(detalleCompraRepository.findByCompraId(50L)).thenReturn(List.of());
        CompraUpdateDTO cambio = new CompraUpdateDTO();
        cambio.setMetodoPago("CREDITO");
        cambio.setTotal(100.0);

        CompraResponseDTO respuesta = service.actualizar(50L, cambio);

        assertEquals("BORRADOR", respuesta.getEstado());
        verify(cuentaPorPagarRepository, never()).save(any(CuentaPorPagarModel.class));
        verify(insumoRepository, never()).save(any(InsumoModel.class));
    }

    @Test
    void borradorNoPermiteRecepcionNiMovimientoDeStock() {
        CompraModel borrador = borradorCredito();
        when(compraRepository.findById(50L)).thenReturn(Optional.of(borrador));

        assertThrows(ValidationException.class, () -> service.recibirCompra(50L));

        verify(detalleCompraRepository, never()).findByCompraId(50L);
        verify(insumoRepository, never()).save(any(InsumoModel.class));
        verify(kardexService, never()).registrarEntradaCompra(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void confirmarBorradorPasaAPendienteYCreaCuentaPorPagarSinMoverStock() {
        CompraModel borrador = borradorCredito();
        DetalleCompraModel detalle = detalle(borrador);
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(borrador));
        when(compraRepository.save(borrador)).thenReturn(borrador);
        when(detalleCompraService.calcularSubtotalValidoBorrador(50L)).thenReturn(100.0);
        when(detalleCompraRepository.findByCompraId(50L)).thenReturn(List.of(detalle));
        when(cuentaPorPagarRepository.findByCompraId(50L)).thenReturn(Optional.empty());

        CompraResponseDTO respuesta = service.confirmarBorrador(50L);

        assertEquals("PENDIENTE", respuesta.getEstado());
        ArgumentCaptor<CuentaPorPagarModel> captor = ArgumentCaptor.forClass(CuentaPorPagarModel.class);
        verify(cuentaPorPagarRepository).save(captor.capture());
        assertEquals(100.0, captor.getValue().getMontoTotal());
        assertEquals(100.0, captor.getValue().getSaldoPendiente());
        assertEquals("PENDIENTE", captor.getValue().getEstado());
        verify(insumoRepository, never()).save(any(InsumoModel.class));
        verify(kardexService, never()).registrarEntradaCompra(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void confirmarRechazaCompraQueYaNoEsBorrador() {
        CompraModel compra = borradorCredito();
        compra.setEstado("PENDIENTE");
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(compra));

        assertThrows(ValidationException.class, () -> service.confirmarBorrador(50L));
        verify(cuentaPorPagarRepository, never()).save(any(CuentaPorPagarModel.class));
    }

    @Test
    void actualizarBorradorReemplazaDetallesYRecalculaTotalesEnServidor() {
        CompraModel borrador = borradorCredito();
        CompraUpdateDTO cambio = new CompraUpdateDTO();
        cambio.setEstado("BORRADOR");
        cambio.setImpuesto(16.0);
        cambio.setSubtotal(999.0);
        cambio.setTotal(999.0);
        cambio.setDetalles(List.of(detalleDto(1L, 2.0, 1.0, 50.0)));
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(borrador));
        when(detalleCompraService.reemplazarDetallesEditables(50L, cambio.getDetalles())).thenReturn(100.0);
        when(compraRepository.save(borrador)).thenReturn(borrador);
        when(cuentaPorPagarRepository.findByCompraId(50L)).thenReturn(Optional.empty());
        when(detalleCompraRepository.findByCompraId(50L)).thenReturn(List.of());

        CompraResponseDTO respuesta = service.actualizar(50L, cambio);

        assertEquals(100.0, respuesta.getSubtotal());
        assertEquals(16.0, respuesta.getImpuesto());
        assertEquals(116.0, respuesta.getTotal());
        verify(detalleCompraService).reemplazarDetallesEditables(50L, cambio.getDetalles());
        verify(cuentaPorPagarRepository, never()).save(any(CuentaPorPagarModel.class));
    }

    @Test
    void actualizarBorradorNoPermiteSaltarAEstadoPendiente() {
        CompraModel borrador = borradorCredito();
        CompraUpdateDTO cambio = new CompraUpdateDTO();
        cambio.setEstado("PENDIENTE");
        cambio.setDetalles(List.of(detalleDto(1L, 2.0, 1.0, 50.0)));
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(borrador));

        assertThrows(ValidationException.class, () -> service.actualizar(50L, cambio));

        verify(detalleCompraService, never()).reemplazarDetallesEditables(any(), any());
        verify(compraRepository, never()).save(any(CompraModel.class));
    }

    @Test
    void actualizarPendienteReemplazaDetallesYDerivaTotalesDelServidor() {
        CompraModel pendiente = borradorCredito();
        pendiente.setEstado("PENDIENTE");
        pendiente.setMetodoPago("CONTADO");
        CompraUpdateDTO cambio = new CompraUpdateDTO();
        cambio.setObservaciones("Cabecera actualizada");
        cambio.setImpuesto(16.0);
        cambio.setSubtotal(999.0);
        cambio.setTotal(999.0);
        cambio.setDetalles(List.of(detalleDto(1L, 3.0, 1.0, 20.0)));
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(pendiente));
        when(detalleCompraService.reemplazarDetallesEditables(50L, cambio.getDetalles())).thenReturn(60.0);
        when(compraRepository.save(pendiente)).thenReturn(pendiente);
        when(cuentaPorPagarRepository.findByCompraId(50L)).thenReturn(Optional.empty());
        when(detalleCompraRepository.findByCompraId(50L)).thenReturn(List.of());

        CompraResponseDTO respuesta = service.actualizar(50L, cambio);

        assertEquals("Cabecera actualizada", respuesta.getObservaciones());
        assertEquals(60.0, respuesta.getSubtotal());
        assertEquals(16.0, respuesta.getImpuesto());
        assertEquals(76.0, respuesta.getTotal());
        verify(detalleCompraService).reemplazarDetallesEditables(50L, cambio.getDetalles());
    }

    @Test
    void confirmarRechazaMetodoDePagoVacioSinCrearCuenta() {
        CompraModel borrador = borradorCredito();
        borrador.setMetodoPago("   ");
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(borrador));

        assertThrows(ValidationException.class, () -> service.confirmarBorrador(50L));

        assertEquals("BORRADOR", borrador.getEstado());
        verify(detalleCompraService, never()).calcularSubtotalValidoBorrador(any());
        verify(cuentaPorPagarRepository, never()).save(any(CuentaPorPagarModel.class));
    }

    @Test
    void confirmarRechazaDetalleInvalidoSinCambiarEstadoNiCrearCuenta() {
        CompraModel borrador = borradorCredito();
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(borrador));
        when(detalleCompraService.calcularSubtotalValidoBorrador(50L))
                .thenThrow(new ValidationException("Detalle inválido"));

        assertThrows(ValidationException.class, () -> service.confirmarBorrador(50L));

        assertEquals("BORRADOR", borrador.getEstado());
        verify(compraRepository, never()).save(any(CompraModel.class));
        verify(cuentaPorPagarRepository, never()).save(any(CuentaPorPagarModel.class));
    }

    @Test
    void confirmarRechazaTotalesQueNoCoincidenConDetalles() {
        CompraModel borrador = borradorCredito();
        borrador.setTotal(90.0);
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(borrador));
        when(detalleCompraService.calcularSubtotalValidoBorrador(50L)).thenReturn(100.0);

        assertThrows(ValidationException.class, () -> service.confirmarBorrador(50L));

        assertEquals("BORRADOR", borrador.getEstado());
        verify(compraRepository, never()).save(any(CompraModel.class));
        verify(cuentaPorPagarRepository, never()).save(any(CuentaPorPagarModel.class));
    }

    @Test
    void confirmarContadoNoCreaCuentaPorPagar() {
        CompraModel borrador = borradorCredito();
        borrador.setMetodoPago("CONTADO");
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(borrador));
        when(detalleCompraService.calcularSubtotalValidoBorrador(50L)).thenReturn(100.0);
        when(compraRepository.save(borrador)).thenReturn(borrador);
        when(cuentaPorPagarRepository.findByCompraId(50L)).thenReturn(Optional.empty());
        when(detalleCompraRepository.findByCompraId(50L)).thenReturn(List.of(detalle(borrador)));

        CompraResponseDTO respuesta = service.confirmarBorrador(50L);

        assertEquals("PENDIENTE", respuesta.getEstado());
        verify(cuentaPorPagarRepository, never()).save(any(CuentaPorPagarModel.class));
        verify(insumoRepository, never()).save(any(InsumoModel.class));
    }

    @Test
    void eliminarCompraCancelaYDesactivaCuentaPorPagarConPagoParcial() {
        CompraModel compra = borradorCredito();
        compra.setEstado("PENDIENTE");
        CuentaPorPagarModel cuenta = CuentaPorPagarModel.builder()
                .id(8L)
                .compra(compra)
                .proveedor(compra.getProveedor())
                .montoTotal(100.0)
                .montoPagado(40.0)
                .saldoPendiente(60.0)
                .estado("PARCIAL")
                .activo(true)
                .build();
        when(compraRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(compra));
        when(compraRepository.save(compra)).thenReturn(compra);
        when(cuentaPorPagarRepository.findByCompraId(50L)).thenReturn(Optional.of(cuenta));

        service.eliminar(50L);

        assertFalse(compra.getActivo());
        assertFalse(cuenta.getActivo());
        assertEquals("CANCELADA", cuenta.getEstado());
        assertEquals(40.0, cuenta.getMontoPagado());
        assertEquals(60.0, cuenta.getSaldoPendiente());
        verify(cuentaPorPagarRepository).save(cuenta);
    }

    private CompraModel borradorCredito() {
        ProveedorModel proveedor = new ProveedorModel();
        proveedor.setId(7L);
        proveedor.setRazonSocial("Proveedor Prueba");
        proveedor.setActivo(true);
        return CompraModel.builder()
                .id(50L)
                .folio("BOR-50")
                .fechaCompra(LocalDate.now())
                .proveedor(proveedor)
                .metodoPago("CREDITO")
                .subtotal(100.0)
                .impuesto(0.0)
                .total(100.0)
                .estado("BORRADOR")
                .activo(true)
                .build();
    }

    private DetalleCompraModel detalle(CompraModel compra) {
        UnidadMedidaModel unidad = new UnidadMedidaModel();
        unidad.setId(1L);
        unidad.setNombre("Pieza");
        unidad.setSimbolo("pz");
        InsumoModel insumo = InsumoModel.builder()
                .id(1L)
                .codigo("I-1")
                .nombre("Insumo")
                .unidadMedida(unidad)
                .stockActual(3.0)
                .stockApartado(0.0)
                .activo(true)
                .build();
        return DetalleCompraModel.builder()
                .id(1L)
                .compra(compra)
                .insumo(insumo)
                .unidadCompra(unidad)
                .cantidad(2.0)
                .cantidadRecibida(0.0)
                .factorConversion(1.0)
                .precioUnitario(50.0)
                .subtotal(100.0)
                .build();
    }

    private DetalleCompraCreateDTO detalleDto(
            Long insumoId,
            double cantidad,
            double factorConversion,
            double precioUnitario) {
        DetalleCompraCreateDTO dto = new DetalleCompraCreateDTO();
        dto.setInsumoId(insumoId);
        dto.setUnidadCompraId(1L);
        dto.setCantidad(cantidad);
        dto.setFactorConversion(factorConversion);
        dto.setPrecioUnitario(precioUnitario);
        return dto;
    }
}
