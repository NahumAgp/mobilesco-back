package com.mobilesco.mobilesco_back.modules.compra.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.CuentaPorPagarModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.PagoCuentaPorPagarModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CuentaPorPagarResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.PagoCuentaPorPagarCreateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CuentaPorPagarRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.PagoCuentaPorPagarRepository;
import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

class CuentaPorPagarServiceTest {

    private CuentaPorPagarRepository cuentaRepository;
    private PagoCuentaPorPagarRepository pagoRepository;
    private CuentaPorPagarService service;

    @BeforeEach
    void setUp() {
        cuentaRepository = mock(CuentaPorPagarRepository.class);
        pagoRepository = mock(PagoCuentaPorPagarRepository.class);
        DetalleCompraRepository detalleRepository = mock(DetalleCompraRepository.class);
        service = new CuentaPorPagarService(cuentaRepository, pagoRepository, detalleRepository);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarPagoParcialActualizaSaldoEstadoYAuditoria() {
        CuentaPorPagarModel cuenta = cuenta(1000.0, 100.0, 900.0, "PARCIAL");
        PagoCuentaPorPagarCreateDTO dto = pago(250.0);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tesoreria@mobilesco.test", "n/a"));

        when(cuentaRepository.findById(50L)).thenReturn(Optional.of(cuenta));
        when(pagoRepository.findByCuentaPorPagarIdOrderByFechaPagoDescIdDesc(50L))
                .thenReturn(List.of());

        CuentaPorPagarResponseDTO response = service.registrarPago(50L, dto);

        assertEquals(350.0, response.getMontoPagado());
        assertEquals(650.0, response.getSaldoPendiente());
        assertEquals("PARCIAL", response.getEstado());
        verify(cuentaRepository).save(cuenta);

        ArgumentCaptor<PagoCuentaPorPagarModel> captor = ArgumentCaptor.forClass(PagoCuentaPorPagarModel.class);
        verify(pagoRepository).save(captor.capture());
        assertEquals(250.0, captor.getValue().getMonto());
        assertEquals("TRANSFERENCIA", captor.getValue().getMetodoPago());
        assertEquals("tesoreria@mobilesco.test", captor.getValue().getUsuario());
    }

    @Test
    void registrarPagoTotalMarcaCuentaPagada() {
        CuentaPorPagarModel cuenta = cuenta(1000.0, 200.0, 800.0, "PARCIAL");
        when(cuentaRepository.findById(50L)).thenReturn(Optional.of(cuenta));
        when(pagoRepository.findByCuentaPorPagarIdOrderByFechaPagoDescIdDesc(50L))
                .thenReturn(List.of());

        CuentaPorPagarResponseDTO response = service.registrarPago(50L, pago(800.0));

        assertEquals(0.0, response.getSaldoPendiente());
        assertEquals("PAGADA", response.getEstado());
    }

    @Test
    void rechazaPagoMayorAlSaldoSinPersistir() {
        CuentaPorPagarModel cuenta = cuenta(1000.0, 800.0, 200.0, "PARCIAL");
        when(cuentaRepository.findById(50L)).thenReturn(Optional.of(cuenta));

        assertThrows(ValidationException.class, () -> service.registrarPago(50L, pago(200.01)));

        verify(pagoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(cuentaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaCuentaInexistente() {
        when(cuentaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.registrarPago(404L, pago(100.0)));
    }

    private CuentaPorPagarModel cuenta(double total, double pagado, double saldo, String estado) {
        ProveedorModel proveedor = new ProveedorModel();
        proveedor.setId(8L);
        proveedor.setRazonSocial("Proveedor de prueba");
        proveedor.setRfc("PDE010101AA1");

        CompraModel compra = CompraModel.builder()
                .id(101L)
                .folio("CMP-TEST-001")
                .fechaCompra(LocalDate.of(2026, 7, 15))
                .proveedor(proveedor)
                .metodoPago("CREDITO")
                .total(total)
                .build();

        return CuentaPorPagarModel.builder()
                .id(50L)
                .compra(compra)
                .proveedor(proveedor)
                .fechaCuenta(LocalDate.of(2026, 7, 15))
                .montoTotal(total)
                .montoPagado(pagado)
                .saldoPendiente(saldo)
                .estado(estado)
                .activo(true)
                .build();
    }

    private PagoCuentaPorPagarCreateDTO pago(double monto) {
        PagoCuentaPorPagarCreateDTO dto = new PagoCuentaPorPagarCreateDTO();
        dto.setMonto(monto);
        dto.setFechaPago(LocalDate.of(2026, 7, 30));
        dto.setMetodoPago(" TRANSFERENCIA ");
        dto.setReferencia(" E2E-REF ");
        return dto;
    }
}
