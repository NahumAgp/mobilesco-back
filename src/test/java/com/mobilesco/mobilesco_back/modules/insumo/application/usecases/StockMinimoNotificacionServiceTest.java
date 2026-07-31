package com.mobilesco.mobilesco_back.modules.insumo.application.usecases;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.notificacion.application.usecases.NotificacionService;
import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.TipoNotificacion;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

class StockMinimoNotificacionServiceTest {

    private NotificacionService notificacionService;
    private StockMinimoNotificacionService service;

    @BeforeEach
    void setUp() {
        notificacionService = mock(NotificacionService.class);
        service = new StockMinimoNotificacionService(notificacionService);
    }

    @Test
    void notificaALosRolesOperativosCuandoElStockCruzaElMinimo() {
        InsumoModel insumo = insumo(8.0, 10.0);

        service.notificarSiCruzaMinimo(insumo, 12.0, 8.0);

        verify(notificacionService).notificarRoles(
                eq(Set.of("ADMIN", "DIRECTOR_GENERAL", "SUBDIRECCION_ADMINISTRATIVA", "JEFE_ALMACEN")),
                eq(TipoNotificacion.ALERTA),
                eq("Stock mínimo alcanzado"),
                contains("Stock actual: 8.00 pz; mínimo: 10.00 pz"),
                eq("ALMACEN"),
                eq("INSUMO"),
                eq(15L),
                eq("/insumos/15"));
    }

    @Test
    void noRepiteNotificacionSiElInsumoYaEstabaBajo() {
        InsumoModel insumo = insumo(6.0, 10.0);

        service.notificarSiCruzaMinimo(insumo, 8.0, 6.0);

        verify(notificacionService, never()).notificarRoles(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void notificaCuandoUnCambioDeMinimoCreaLaCondicionDeAlerta() {
        InsumoModel insumo = insumo(8.0, 10.0);

        service.notificarSiCambioMinimoGeneraAlerta(insumo, 5.0);

        verify(notificacionService).notificarRoles(
                org.mockito.ArgumentMatchers.any(),
                eq(TipoNotificacion.ALERTA),
                eq("Stock mínimo alcanzado"),
                org.mockito.ArgumentMatchers.any(),
                eq("ALMACEN"),
                eq("INSUMO"),
                eq(15L),
                eq("/insumos/15"));
    }

    @Test
    void ignoraMinimosEnCeroParaNoGenerarAlertasDeConfiguracionInicial() {
        InsumoModel insumo = insumo(0.0, 0.0);

        service.notificarSiNuevoInsumoIniciaBajo(insumo);

        verify(notificacionService, never()).notificarRoles(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private InsumoModel insumo(double stock, double minimo) {
        UnidadMedidaModel unidad = new UnidadMedidaModel();
        unidad.setSimbolo("pz");
        return InsumoModel.builder()
                .id(15L)
                .codigo("7500000000015")
                .nombre("Tornillo 1/4")
                .unidadMedida(unidad)
                .stockActual(stock)
                .stockMinimo(minimo)
                .activo(true)
                .build();
    }
}
