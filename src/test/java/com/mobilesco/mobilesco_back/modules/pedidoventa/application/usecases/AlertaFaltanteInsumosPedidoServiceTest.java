package com.mobilesco.mobilesco_back.modules.pedidoventa.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.notificacion.application.usecases.NotificacionService;
import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.TipoNotificacion;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.out.persistence.repositories.OrdenProduccionRepository;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.EstadoPedidoVenta;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.PedidoVentaDetalleModel;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.PedidoVentaModel;
import com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.out.persistence.repositories.PedidoVentaRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

@ExtendWith(MockitoExtension.class)
class AlertaFaltanteInsumosPedidoServiceTest {

    @Mock PedidoVentaRepository pedidoRepository;
    @Mock OrdenProduccionRepository ordenRepository;
    @Mock ProductoInsumoRepository productoInsumoRepository;
    @Mock NotificacionService notificacionService;
    @InjectMocks AlertaFaltanteInsumosPedidoService service;

    @Test
    void notificaCompraPrioritariaAlAdminConLaCantidadFaltante() {
        UnidadMedidaModel unidad = new UnidadMedidaModel();
        unidad.setSimbolo("m");
        InsumoModel tela = InsumoModel.builder()
                .id(4L).codigo("TELA-01").nombre("Tela gris").unidadMedida(unidad)
                .stockActual(2.0).stockApartado(1.0).activo(true).build();
        ProductoModel silla = ProductoModel.builder().id(7L).sku("SILLA-01").nombre("Silla").build();
        ProductoInsumoModel consumo = ProductoInsumoModel.builder()
                .producto(silla).insumo(tela).cantidad(2.0).desperdicioPorcentaje(0.0).build();
        PedidoVentaModel pedido = PedidoVentaModel.builder()
                .id(12L).folio("PV-2026-00012").estado(EstadoPedidoVenta.EN_PRODUCCION)
                .detalles(new ArrayList<>()).build();
        pedido.agregarDetalle(PedidoVentaDetalleModel.builder()
                .producto(silla).skuSnapshot("SILLA-01").nombreSnapshot("Silla")
                .cantidadSolicitada(new BigDecimal("10")).cantidadReservada(BigDecimal.ZERO)
                .cantidadEntregada(BigDecimal.ZERO).cantidadProducida(BigDecimal.ZERO)
                .precioUnitario(BigDecimal.ZERO).importe(BigDecimal.ZERO).build());

        when(pedidoRepository.findById(12L)).thenReturn(Optional.of(pedido));
        when(ordenRepository.findFirstByPedidoVentaIdAndEstadoNotOrderByIdDesc(eq(12L), any()))
                .thenReturn(Optional.empty());
        when(productoInsumoRepository.findByProductoId(7L)).thenReturn(List.of(consumo));
        when(notificacionService.notificarRolesSiNoExiste(
                anySet(), any(), any(), any(), any(), any(), anyLong(), any()))
                .thenReturn(1);

        int creadas = service.notificarFaltantes(12L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> roles = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<String> mensaje = ArgumentCaptor.forClass(String.class);
        verify(notificacionService).notificarRolesSiNoExiste(
                roles.capture(),
                eq(TipoNotificacion.ACCION_REQUERIDA),
                eq("Compra prioritaria requerida"),
                mensaje.capture(),
                eq("COMPRAS"),
                eq("PEDIDO_VENTA"),
                eq(12L),
                eq("/pedidos-venta"));
        assertThat(creadas).isEqualTo(1);
        assertThat(roles.getValue()).contains("ADMIN", "JEFE_ALMACEN", "SUPERVISOR_PRODUCCION");
        assertThat(mensaje.getValue()).contains("PV-2026-00012", "Tela gris", "faltan 19.000 m");
    }
}
