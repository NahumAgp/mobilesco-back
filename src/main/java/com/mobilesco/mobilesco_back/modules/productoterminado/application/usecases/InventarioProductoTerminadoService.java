package com.mobilesco.mobilesco_back.modules.productoterminado.application.usecases;

import java.math.BigDecimal;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.*;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.*;
import com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.out.persistence.repositories.PedidoVentaDetalleRepository;
import com.mobilesco.mobilesco_back.modules.productoterminado.domain.models.*;
import com.mobilesco.mobilesco_back.modules.productoterminado.infrastructure.in.api.dtos.InventarioProductoTerminadoResponseDTO;
import com.mobilesco.mobilesco_back.modules.productoterminado.infrastructure.out.persistence.repositories.*;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class InventarioProductoTerminadoService {
    private final InventarioProductoTerminadoRepository inventarioRepository;
    private final MovimientoProductoTerminadoRepository movimientoRepository;
    private final PedidoVentaDetalleRepository pedidoDetalleRepository;

    @Transactional(readOnly = true)
    public PageResponseDTO<InventarioProductoTerminadoResponseDTO> listar(String busqueda, Pageable pageable) {
        String texto = busqueda == null || busqueda.isBlank() ? null : busqueda.trim();
        Page<InventarioProductoTerminadoResponseDTO> page = inventarioRepository.buscar(texto, pageable).map(this::mapear);
        return new PageResponseDTO<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public void reservarDisponibles(PedidoVentaModel pedido) {
        for (PedidoVentaDetalleModel detalle : pedido.getDetalles()) {
            InventarioProductoTerminadoModel inventario = obtenerOCrear(detalle);
            BigDecimal disponible = inventario.getExistencia().subtract(inventario.getReservado()).max(BigDecimal.ZERO);
            BigDecimal pendiente = detalle.getCantidadSolicitada().subtract(detalle.getCantidadEntregada()).subtract(detalle.getCantidadReservada()).max(BigDecimal.ZERO);
            BigDecimal reserva = disponible.min(pendiente);
            if (reserva.signum() > 0) {
                inventario.setReservado(inventario.getReservado().add(reserva));
                detalle.setCantidadReservada(detalle.getCantidadReservada().add(reserva));
                inventarioRepository.save(inventario);
            }
        }
    }

    @Transactional
    public void liberarReservas(PedidoVentaModel pedido) {
        for (PedidoVentaDetalleModel detalle : pedido.getDetalles()) {
            if (detalle.getCantidadReservada().signum() <= 0) continue;
            InventarioProductoTerminadoModel inventario = obtenerOCrear(detalle);
            inventario.setReservado(inventario.getReservado().subtract(detalle.getCantidadReservada()).max(BigDecimal.ZERO));
            detalle.setCantidadReservada(BigDecimal.ZERO.setScale(3));
            inventarioRepository.save(inventario);
        }
    }

    @Transactional
    public void registrarEntradaProduccion(OrdenProduccionModel orden, OrdenProduccionDetalleModel partida,
            BigDecimal cantidad, Long avanceId, String usuario) {
        if (movimientoRepository.existsByTipoAndReferenciaId(TipoMovimientoProductoTerminado.ENTRADA_PRODUCCION, avanceId)) return;
        InventarioProductoTerminadoModel inventario = inventarioRepository.findByProductoIdForUpdate(partida.getProducto().getId())
                .orElseGet(() -> inventarioRepository.saveAndFlush(InventarioProductoTerminadoModel.builder()
                        .producto(partida.getProducto()).existencia(BigDecimal.ZERO.setScale(3))
                        .reservado(BigDecimal.ZERO.setScale(3)).build()));
        BigDecimal anterior = inventario.getExistencia();
        inventario.setExistencia(anterior.add(cantidad));
        PedidoVentaDetalleModel detallePedido = partida.getPedidoVentaDetalle();
        PedidoVentaModel pedido = orden.getPedidoVenta();
        if (detallePedido != null) {
            detallePedido.setCantidadProducida(detallePedido.getCantidadProducida().add(cantidad));
            BigDecimal pendienteReserva = detallePedido.getCantidadSolicitada().subtract(detallePedido.getCantidadEntregada())
                    .subtract(detallePedido.getCantidadReservada()).max(BigDecimal.ZERO);
            BigDecimal reserva = cantidad.min(pendienteReserva);
            detallePedido.setCantidadReservada(detallePedido.getCantidadReservada().add(reserva));
            inventario.setReservado(inventario.getReservado().add(reserva));
            pedidoDetalleRepository.save(detallePedido);
        }
        inventarioRepository.save(inventario);
        movimientoRepository.save(MovimientoProductoTerminadoModel.builder().inventario(inventario)
                .tipo(TipoMovimientoProductoTerminado.ENTRADA_PRODUCCION).cantidad(cantidad)
                .existenciaAnterior(anterior).existenciaNueva(inventario.getExistencia()).ordenProduccion(orden)
                .pedidoVenta(pedido).referenciaId(avanceId).observaciones("Avance de producción " + orden.getFolio())
                .usuario(usuario).build());
    }

    private InventarioProductoTerminadoModel obtenerOCrear(PedidoVentaDetalleModel detalle) {
        return inventarioRepository.findByProductoIdForUpdate(detalle.getProducto().getId())
                .orElseGet(() -> inventarioRepository.saveAndFlush(InventarioProductoTerminadoModel.builder()
                        .producto(detalle.getProducto()).existencia(BigDecimal.ZERO.setScale(3))
                        .reservado(BigDecimal.ZERO.setScale(3)).build()));
    }

    private InventarioProductoTerminadoResponseDTO mapear(InventarioProductoTerminadoModel i) {
        return InventarioProductoTerminadoResponseDTO.builder().id(i.getId()).productoId(i.getProducto().getId())
                .sku(i.getProducto().getSku()).nombre(i.getProducto().getNombre()).existencia(i.getExistencia())
                .reservado(i.getReservado()).disponible(i.getExistencia().subtract(i.getReservado()).max(BigDecimal.ZERO))
                .fechaActualizacion(i.getFechaActualizacion()).build();
    }
}
