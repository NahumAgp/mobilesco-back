package com.mobilesco.mobilesco_back.modules.insumo.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.out.persistence.repositories.KardexRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories.UnidadMedidaRepository;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories.DetalleSalidaInsumoRepository;

class InsumoServiceTest {

    private InsumoRepository insumoRepository;
    private UnidadMedidaRepository unidadMedidaRepository;
    private KardexService kardexService;
    private DetalleCompraRepository detalleCompraRepository;
    private ProductoInsumoRepository productoInsumoRepository;
    private KardexRepository kardexRepository;
    private DetalleSalidaInsumoRepository detalleSalidaInsumoRepository;
    private InsumoService service;

    @BeforeEach
    void setUp() {
        insumoRepository = mock(InsumoRepository.class);
        unidadMedidaRepository = mock(UnidadMedidaRepository.class);
        kardexService = mock(KardexService.class);
        detalleCompraRepository = mock(DetalleCompraRepository.class);
        productoInsumoRepository = mock(ProductoInsumoRepository.class);
        kardexRepository = mock(KardexRepository.class);
        detalleSalidaInsumoRepository = mock(DetalleSalidaInsumoRepository.class);

        service = new InsumoService(
                insumoRepository,
                unidadMedidaRepository,
                kardexService,
                detalleCompraRepository,
                productoInsumoRepository,
                kardexRepository,
                detalleSalidaInsumoRepository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "tester",
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        when(detalleCompraRepository.findUltimasComprasRecibidasByInsumo(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of());
        when(kardexService.calcularCostoPromedio(1L)).thenReturn(0.0);
        when(detalleCompraRepository.existsByInsumoId(1L)).thenReturn(false);
        when(productoInsumoRepository.existsByInsumoId(1L)).thenReturn(false);
        when(kardexRepository.existsByInsumoId(1L)).thenReturn(false);
        when(detalleSalidaInsumoRepository.existsByInsumoId(1L)).thenReturn(false);
        when(insumoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ajustarStockEntradaActualizaStockYRegistraKardex() {
        UnidadMedidaModel unidad = new UnidadMedidaModel();
        unidad.setId(7L);
        unidad.setNombre("Pieza");
        unidad.setSimbolo("pz");
        unidad.setEstado(true);

        InsumoModel insumo = InsumoModel.builder()
                .id(1L)
                .codigo("INS-001")
                .nombre("Tornillo")
                .unidadMedida(unidad)
                .stockActual(10.0)
                .stockMinimo(2.0)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

        var response = service.ajustarStock(1L, 5.0, "ENTRADA", "Inventario inicial");

        ArgumentCaptor<InsumoModel> captor = ArgumentCaptor.forClass(InsumoModel.class);
        verify(insumoRepository).save(captor.capture());
        assertEquals(15.0, captor.getValue().getStockActual(), 0.0001);
        assertEquals(15.0, response.getStockActual(), 0.0001);
        verify(kardexService).registrarAjuste(1L, 10.0, 15.0, "Inventario inicial", "tester");
    }

    @Test
    void ajustarStockSalidaRechazaCuandoNoHaySuficienteStock() {
        UnidadMedidaModel unidad = new UnidadMedidaModel();
        unidad.setId(7L);
        unidad.setNombre("Pieza");
        unidad.setSimbolo("pz");
        unidad.setEstado(true);

        InsumoModel insumo = InsumoModel.builder()
                .id(1L)
                .codigo("INS-001")
                .nombre("Tornillo")
                .unidadMedida(unidad)
                .stockActual(3.0)
                .stockMinimo(2.0)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

        assertThrows(ValidationException.class, () -> service.ajustarStock(1L, 5.0, "SALIDA", "Ajuste"));
        verifyNoInteractions(kardexService);
    }

    @Test
    void ajustarStockRechazaMotivoVacio() {
        assertThrows(
                ValidationException.class,
                () -> service.ajustarStock(1L, 1.0, "ENTRADA", "   "));

        verifyNoInteractions(insumoRepository, kardexService);
    }
}
