package com.mobilesco.mobilesco_back.modules.compra.application.usecases;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.out.persistence.repositories.ProveedorRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

class CompraServiceTest {

    private CompraRepository compraRepository;
    private CompraService service;

    @BeforeEach
    void setUp() {
        compraRepository = mock(CompraRepository.class);
        ProveedorRepository proveedorRepository = mock(ProveedorRepository.class);
        InsumoRepository insumoRepository = mock(InsumoRepository.class);
        DetalleCompraRepository detalleCompraRepository = mock(DetalleCompraRepository.class);
        DetalleCompraService detalleCompraService = mock(DetalleCompraService.class);
        KardexService kardexService = mock(KardexService.class);

        service = new CompraService(
                compraRepository,
                proveedorRepository,
                insumoRepository,
                detalleCompraRepository,
                detalleCompraService,
                kardexService);
    }

    @Test
    void eliminarDesactivaCompraPendiente() {
        CompraModel compra = CompraModel.builder()
                .id(10L)
                .folio("CMP-10")
                .estado("PENDIENTE")
                .activo(true)
                .build();
        when(compraRepository.findById(10L)).thenReturn(Optional.of(compra));

        service.eliminar(10L);

        assertFalse(compra.getActivo());
        verify(compraRepository).save(compra);
    }

    @Test
    void eliminarNoPermiteCompraConMovimientoRecibido() {
        CompraModel compra = CompraModel.builder()
                .id(11L)
                .estado("RECIBIDA")
                .activo(true)
                .build();
        when(compraRepository.findById(11L)).thenReturn(Optional.of(compra));

        assertThrows(ValidationException.class, () -> service.eliminar(11L));
        verify(compraRepository, never()).save(compra);
    }
}
