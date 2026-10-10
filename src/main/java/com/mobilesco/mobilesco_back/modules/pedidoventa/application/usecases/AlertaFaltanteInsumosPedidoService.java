package com.mobilesco.mobilesco_back.modules.pedidoventa.application.usecases;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.notificacion.application.usecases.NotificacionService;
import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.TipoNotificacion;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.EstadoOrdenProduccion;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.OrdenProduccionModel;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.out.persistence.repositories.OrdenProduccionRepository;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.PedidoVentaDetalleModel;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.PedidoVentaModel;
import com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.out.persistence.repositories.PedidoVentaRepository;
import com.mobilesco.mobilesco_back.modules.producto.application.usecases.DespieceProducto;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertaFaltanteInsumosPedidoService {

    static final String TITULO = "Compra prioritaria requerida";
    private static final Set<String> ROLES_DESTINATARIOS = Set.of(
            "ADMIN",
            "DIRECTOR_GENERAL",
            "SUBDIRECCION_ADMINISTRATIVA",
            "JEFE_ALMACEN",
            "SUPERVISOR_PRODUCCION");

    private final PedidoVentaRepository pedidoRepository;
    private final OrdenProduccionRepository ordenRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final NotificacionService notificacionService;

    @Transactional
    public int notificarFaltantes(Long pedidoId) {
        PedidoVentaModel pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venta no encontrado"));
        List<Faltante> faltantes = calcularFaltantes(pedido);
        if (faltantes.isEmpty()) {
            return 0;
        }

        String mensaje = construirMensaje(pedido, faltantes);
        return notificacionService.notificarRolesSiNoExiste(
                ROLES_DESTINATARIOS,
                TipoNotificacion.ACCION_REQUERIDA,
                TITULO,
                mensaje,
                "COMPRAS",
                "PEDIDO_VENTA",
                pedido.getId(),
                "/pedidos-venta");
    }

    private List<Faltante> calcularFaltantes(PedidoVentaModel pedido) {
        Optional<OrdenProduccionModel> ordenActiva = ordenRepository
                .findFirstByPedidoVentaIdAndEstadoNotOrderByIdDesc(pedido.getId(), EstadoOrdenProduccion.CANCELADA);
        if (ordenActiva.isPresent() && ordenActiva.get().getEstado() != EstadoOrdenProduccion.BORRADOR) {
            return faltantesOrdenLiberada(ordenActiva.get());
        }
        return faltantesDesdeBom(pedido);
    }

    private List<Faltante> faltantesOrdenLiberada(OrdenProduccionModel orden) {
        return orden.getInsumos().stream()
                .map(necesidad -> {
                    BigDecimal requeridoPendiente = necesidad.getCantidadRequerida()
                            .subtract(necesidad.getCantidadSurtida());
                    BigDecimal apartado = Optional.ofNullable(necesidad.getCantidadApartada())
                            .orElse(BigDecimal.ZERO);
                    BigDecimal faltante = requeridoPendiente.subtract(apartado).max(BigDecimal.ZERO);
                    return new Faltante(necesidad.getInsumo(), faltante);
                })
                .filter(item -> item.cantidad().signum() > 0)
                .sorted(Comparator.comparing(item -> item.insumo().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<Faltante> faltantesDesdeBom(PedidoVentaModel pedido) {
        Map<Long, Requerimiento> requerimientos = new LinkedHashMap<>();
        for (PedidoVentaDetalleModel detalle : pedido.getDetalles()) {
            BigDecimal pendiente = detalle.getCantidadSolicitada()
                    .subtract(detalle.getCantidadReservada())
                    .subtract(detalle.getCantidadEntregada())
                    .max(BigDecimal.ZERO);
            if (pendiente.signum() <= 0) {
                continue;
            }
            var bom = productoInsumoRepository.findByProductoId(detalle.getProducto().getId());
            for (var consumo : DespieceProducto.consolidar(bom)) {
                BigDecimal cantidad = consumo.cantidad().multiply(pendiente);
                requerimientos.compute(consumo.insumo().getId(), (id, actual) ->
                        new Requerimiento(
                                consumo.insumo(),
                                actual == null ? cantidad : actual.cantidad().add(cantidad)));
            }
        }

        List<Faltante> faltantes = new ArrayList<>();
        for (Requerimiento requerimiento : requerimientos.values()) {
            InsumoModel insumo = requerimiento.insumo();
            BigDecimal existencia = decimal(insumo.getStockActual());
            BigDecimal apartado = decimal(insumo.getStockApartado());
            BigDecimal disponible = existencia.subtract(apartado).max(BigDecimal.ZERO);
            BigDecimal faltante = requerimiento.cantidad().subtract(disponible).max(BigDecimal.ZERO);
            if (faltante.signum() > 0) {
                faltantes.add(new Faltante(insumo, faltante));
            }
        }
        faltantes.sort(Comparator.comparing(item -> item.insumo().getNombre(), String.CASE_INSENSITIVE_ORDER));
        return faltantes;
    }

    private String construirMensaje(PedidoVentaModel pedido, List<Faltante> faltantes) {
        StringBuilder mensaje = new StringBuilder("El pedido ")
                .append(pedido.getFolio())
                .append(" no cuenta con insumos suficientes. Compra prioritaria: ");
        int limite = Math.min(5, faltantes.size());
        for (int i = 0; i < limite; i++) {
            if (i > 0) {
                mensaje.append("; ");
            }
            Faltante faltante = faltantes.get(i);
            mensaje.append(faltante.insumo().getNombre())
                    .append(" (faltan ")
                    .append(formato(faltante.cantidad()))
                    .append(unidad(faltante.insumo()))
                    .append(')');
        }
        if (faltantes.size() > limite) {
            mensaje.append("; y ").append(faltantes.size() - limite).append(" insumo(s) más");
        }
        return mensaje.append('.').toString();
    }

    private String formato(BigDecimal valor) {
        return String.format(Locale.ROOT, "%.3f", valor.setScale(3, RoundingMode.HALF_UP));
    }

    private String unidad(InsumoModel insumo) {
        return insumo.getUnidadMedida() == null || insumo.getUnidadMedida().getSimbolo() == null
                ? ""
                : " " + insumo.getUnidadMedida().getSimbolo();
    }

    private BigDecimal decimal(Double valor) {
        return BigDecimal.valueOf(valor == null ? 0 : valor);
    }

    private record Requerimiento(InsumoModel insumo, BigDecimal cantidad) {}
    private record Faltante(InsumoModel insumo, BigDecimal cantidad) {}
}
