package com.mobilesco.mobilesco_back.modules.pedidoventa.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.*;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.*;
import com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.in.api.dtos.PedidoVentaResponseDTO;
import com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.out.persistence.repositories.PedidoVentaRepository;
import com.mobilesco.mobilesco_back.modules.productoterminado.application.usecases.InventarioProductoTerminadoService;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.*;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class PedidoVentaService {
    private final PedidoVentaRepository pedidoRepository;
    private final CotizacionRepository cotizacionRepository;
    private final InventarioProductoTerminadoService inventarioService;
    private final AlertaFaltanteInsumosPedidoService alertaFaltantesService;

    @Transactional(readOnly = true)
    public PageResponseDTO<PedidoVentaResponseDTO> listar(EstadoPedidoVenta estado, String busqueda, Pageable pageable) {
        String texto = busqueda == null || busqueda.isBlank() ? null : busqueda.trim();
        Page<PedidoVentaResponseDTO> page = pedidoRepository.buscar(estado, texto, pageable).map(this::mapear);
        return new PageResponseDTO<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PedidoVentaResponseDTO obtener(Long id) { return mapear(buscar(id)); }

    @Transactional
    public PedidoVentaResponseDTO convertirCotizacion(Long cotizacionId, LocalDate fechaCompromiso, String usuario) {
        CotizacionModel cotizacion = cotizacionRepository.findById(cotizacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada"));
        if (cotizacion.getEstado() != EstadoCotizacion.ACEPTADA)
            throw new ValidationException("Sólo una cotización aceptada puede convertirse en pedido de venta");
        if (pedidoRepository.existsByCotizacionId(cotizacionId))
            throw new ValidationException("La cotización ya tiene un pedido de venta");
        if (cotizacion.getCliente() == null)
            throw new ValidationException("La cotización debe tener un cliente antes de convertirse en pedido");

        PedidoVentaModel pedido = PedidoVentaModel.builder()
                .folio("TMP-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                .cotizacion(cotizacion).cliente(cotizacion.getCliente()).estado(EstadoPedidoVenta.BORRADOR)
                .fechaPedido(LocalDate.now()).fechaCompromiso(fechaCompromiso).total(cotizacion.getTotal())
                .notas(cotizacion.getNotas()).creadoPor(usuario).actualizadoPor(usuario).build();
        for (CotizacionDetalleModel item : cotizacion.getDetalles()) {
            pedido.agregarDetalle(PedidoVentaDetalleModel.builder()
                    .producto(item.getProducto()).skuSnapshot(item.getSkuSnapshot()).nombreSnapshot(item.getNombreSnapshot())
                    .cantidadSolicitada(BigDecimal.valueOf(item.getCantidad()).setScale(3))
                    .cantidadProducida(BigDecimal.ZERO.setScale(3)).cantidadReservada(BigDecimal.ZERO.setScale(3))
                    .cantidadEntregada(BigDecimal.ZERO.setScale(3)).precioUnitario(item.getPrecioUnitario())
                    .importe(item.getImporte()).build());
        }
        pedidoRepository.saveAndFlush(pedido);
        pedido.setFolio("PV-" + LocalDate.now().getYear() + "-" + String.format("%05d", pedido.getId()));
        cotizacion.setEstado(EstadoCotizacion.COMPLETADA);
        cotizacionRepository.save(cotizacion);
        return mapear(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoVentaResponseDTO confirmar(Long id, String usuario) {
        PedidoVentaModel pedido = buscar(id);
        if (pedido.getEstado() != EstadoPedidoVenta.BORRADOR)
            throw new ValidationException("Sólo un pedido en borrador puede confirmarse");
        pedido.setEstado(EstadoPedidoVenta.CONFIRMADO);
        pedido.setActualizadoPor(usuario);
        inventarioService.reservarDisponibles(pedido);
        actualizarEstadoListo(pedido);
        pedidoRepository.save(pedido);
        alertaFaltantesService.notificarFaltantes(pedido.getId());
        return mapear(pedido);
    }

    @Transactional
    public PedidoVentaResponseDTO cancelar(Long id, String usuario) {
        PedidoVentaModel pedido = buscar(id);
        if (pedido.getEstado() == EstadoPedidoVenta.CANCELADO || pedido.getEstado() == EstadoPedidoVenta.ENTREGADO)
            throw new ValidationException("El pedido ya no admite cancelación");
        if (pedido.getDetalles().stream().anyMatch(d -> d.getCantidadEntregada().signum() > 0))
            throw new ValidationException("No se puede cancelar un pedido con entregas registradas");
        inventarioService.liberarReservas(pedido);
        pedido.setEstado(EstadoPedidoVenta.CANCELADO);
        pedido.setActualizadoPor(usuario);
        return mapear(pedidoRepository.save(pedido));
    }

    public void actualizarEstadoListo(PedidoVentaModel pedido) {
        boolean completo = pedido.getDetalles().stream()
                .allMatch(d -> d.getCantidadReservada().add(d.getCantidadEntregada()).compareTo(d.getCantidadSolicitada()) >= 0);
        if (completo && pedido.getEstado() != EstadoPedidoVenta.CANCELADO && pedido.getEstado() != EstadoPedidoVenta.ENTREGADO)
            pedido.setEstado(EstadoPedidoVenta.LISTO);
    }

    private PedidoVentaModel buscar(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido de venta no encontrado"));
    }

    private String nombreCliente(PedidoVentaModel pedido) {
        var c = pedido.getCliente();
        if (c == null) return null;
        if (c.getNombreComercial() != null && !c.getNombreComercial().isBlank()) return c.getNombreComercial();
        if (c.getRazonSocial() != null && !c.getRazonSocial().isBlank()) return c.getRazonSocial();
        return c.getNombre();
    }

    private PedidoVentaResponseDTO mapear(PedidoVentaModel pedido) {
        var detalles = pedido.getDetalles().stream().map(d -> PedidoVentaResponseDTO.Detalle.builder()
                .id(d.getId()).productoId(d.getProducto().getId()).sku(d.getSkuSnapshot()).nombre(d.getNombreSnapshot())
                .cantidadSolicitada(d.getCantidadSolicitada()).cantidadProducida(d.getCantidadProducida())
                .cantidadReservada(d.getCantidadReservada()).cantidadEntregada(d.getCantidadEntregada())
                .pendienteProduccion(d.getCantidadSolicitada().subtract(d.getCantidadReservada()).subtract(d.getCantidadEntregada()).max(BigDecimal.ZERO))
                .precioUnitario(d.getPrecioUnitario()).importe(d.getImporte()).build()).toList();
        return PedidoVentaResponseDTO.builder().id(pedido.getId()).folio(pedido.getFolio()).estado(pedido.getEstado().name())
                .cotizacionId(pedido.getCotizacion() == null ? null : pedido.getCotizacion().getId())
                .cotizacionFolio(pedido.getCotizacion() == null ? null : pedido.getCotizacion().getFolio())
                .clienteId(pedido.getCliente() == null ? null : pedido.getCliente().getId()).clienteNombre(nombreCliente(pedido))
                .fechaPedido(pedido.getFechaPedido()).fechaCompromiso(pedido.getFechaCompromiso()).total(pedido.getTotal())
                .notas(pedido.getNotas()).creadoPor(pedido.getCreadoPor()).actualizadoPor(pedido.getActualizadoPor())
                .fechaRegistro(pedido.getFechaRegistro()).fechaActualizacion(pedido.getFechaActualizacion()).detalles(detalles).build();
    }
}
