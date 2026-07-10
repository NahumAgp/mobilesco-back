package com.mobilesco.mobilesco_back.modules.compra.application.usecases;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.CuentaPorPagarModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.PagoCuentaPorPagarModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CompraResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CuentaPorPagarResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.PagoCuentaPorPagarCreateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.PagoCuentaPorPagarResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CuentaPorPagarRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.PagoCuentaPorPagarRepository;
import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CuentaPorPagarService {

    private final CuentaPorPagarRepository cuentaPorPagarRepository;
    private final PagoCuentaPorPagarRepository pagoCuentaPorPagarRepository;
    private final DetalleCompraRepository detalleCompraRepository;

    @Transactional(readOnly = true)
    public List<CuentaPorPagarResponseDTO> listar(String estado) {
        List<CuentaPorPagarModel> cuentas = estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado)
                ? cuentaPorPagarRepository.findByActivoTrueOrderByFechaCuentaDesc()
                : cuentaPorPagarRepository.findByEstadoAndActivoTrueOrderByFechaCuentaDesc(estado.trim().toUpperCase());

        return cuentas.stream()
                .map(cuenta -> mapToResponseDTO(cuenta, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CuentaPorPagarResponseDTO> listarPaginado(String estado, String busqueda, Pageable pageable) {
        return cuentaPorPagarRepository
                .buscarPaginado(normalizarTexto(estado), normalizarTexto(busqueda), pageable)
                .map(cuenta -> mapToResponseDTO(cuenta, false));
    }

    @Transactional(readOnly = true)
    public CuentaPorPagarResponseDTO obtenerPorId(Long id) {
        CuentaPorPagarModel cuenta = cuentaPorPagarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por pagar no encontrada con id: " + id));
        return mapToResponseDTO(cuenta, true);
    }

    @Transactional
    public CuentaPorPagarResponseDTO registrarPago(Long cuentaId, PagoCuentaPorPagarCreateDTO dto) {
        CuentaPorPagarModel cuenta = cuentaPorPagarRepository.findById(cuentaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por pagar no encontrada con id: " + cuentaId));

        if (!Boolean.TRUE.equals(cuenta.getActivo()) || "CANCELADA".equals(cuenta.getEstado())) {
            throw new ValidationException("No se puede registrar pago en una cuenta cancelada o inactiva");
        }

        double saldo = nvl(cuenta.getSaldoPendiente());
        double monto = nvl(dto.getMonto());

        if (monto <= 0) {
            throw new ValidationException("El monto del pago debe ser mayor a 0");
        }

        if (monto > saldo) {
            throw new ValidationException("El monto del pago no puede ser mayor al saldo pendiente");
        }

        PagoCuentaPorPagarModel pago = PagoCuentaPorPagarModel.builder()
                .cuentaPorPagar(cuenta)
                .fechaPago(dto.getFechaPago() != null ? dto.getFechaPago() : LocalDate.now())
                .monto(monto)
                .metodoPago(normalizarTexto(dto.getMetodoPago()))
                .referencia(normalizarTexto(dto.getReferencia()))
                .observaciones(normalizarTexto(dto.getObservaciones()))
                .usuario(obtenerUsuarioAutenticado())
                .build();

        pagoCuentaPorPagarRepository.save(pago);

        double montoPagado = nvl(cuenta.getMontoPagado()) + monto;
        cuenta.setMontoPagado(redondear(montoPagado));
        cuenta.setSaldoPendiente(redondear(nvl(cuenta.getMontoTotal()) - cuenta.getMontoPagado()));
        cuenta.setEstado(resolverEstado(cuenta.getMontoTotal(), cuenta.getMontoPagado()));
        cuentaPorPagarRepository.save(cuenta);

        return mapToResponseDTO(cuenta, true);
    }

    private CuentaPorPagarResponseDTO mapToResponseDTO(CuentaPorPagarModel cuenta, boolean incluirDetalle) {
        CompraModel compra = cuenta.getCompra();
        ProveedorModel proveedor = cuenta.getProveedor();
        List<PagoCuentaPorPagarResponseDTO> pagos = pagoCuentaPorPagarRepository
                .findByCuentaPorPagarIdOrderByFechaPagoDescIdDesc(cuenta.getId())
                .stream()
                .map(this::mapPago)
                .collect(Collectors.toList());

        return CuentaPorPagarResponseDTO.builder()
                .id(cuenta.getId())
                .compraId(compra.getId())
                .compraFolio(compra.getFolio())
                .fechaCompra(compra.getFechaCompra())
                .proveedorId(proveedor.getId())
                .proveedorRazonSocial(proveedor.getRazonSocial())
                .proveedorRfc(proveedor.getRfc())
                .fechaCuenta(cuenta.getFechaCuenta())
                .fechaVencimiento(cuenta.getFechaVencimiento())
                .montoTotal(cuenta.getMontoTotal())
                .montoPagado(cuenta.getMontoPagado())
                .saldoPendiente(cuenta.getSaldoPendiente())
                .estado(cuenta.getEstado())
                .metodoPagoCompra(compra.getMetodoPago())
                .observaciones(cuenta.getObservaciones())
                .activo(cuenta.getActivo())
                .fechaRegistro(cuenta.getFechaRegistro())
                .fechaActualizacion(cuenta.getFechaActualizacion())
                .compra(incluirDetalle ? mapCompra(compra) : null)
                .pagos(pagos)
                .build();
    }

    private CompraResponseDTO mapCompra(CompraModel compra) {
        ProveedorModel proveedor = compra.getProveedor();
        String nombreCompleto = String.join(" ",
                proveedor.getNombre() != null ? proveedor.getNombre() : "",
                proveedor.getApellidoPaterno() != null ? proveedor.getApellidoPaterno() : "",
                proveedor.getApellidoMaterno() != null ? proveedor.getApellidoMaterno() : "").trim();

        List<DetalleCompraResponseDTO> detalles = detalleCompraRepository.findByCompraId(compra.getId())
                .stream()
                .map(detalle -> DetalleCompraResponseDTO.builder()
                        .id(detalle.getId())
                        .insumoId(detalle.getInsumo().getId())
                        .insumoNombre(detalle.getInsumo().getNombre())
                        .cantidad(detalle.getCantidad())
                        .factorConversion(detalle.getFactorConversion())
                        .cantidadRecibida(detalle.getCantidadRecibida())
                        .cantidadEnUnidadConsumo(detalle.getCantidadEnUnidadConsumo())
                        .cantidadPendiente(detalle.getCantidadPendiente())
                        .unidadCompraId(detalle.getUnidadCompra().getId())
                        .unidadCompraSimbolo(detalle.getUnidadCompra().getSimbolo())
                        .unidadConsumoId(detalle.getInsumo().getUnidadMedida().getId())
                        .unidadConsumoSimbolo(detalle.getInsumo().getUnidadMedida().getSimbolo())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .costoPorUnidadConsumo(detalle.getCostoPorUnidadConsumo())
                        .subtotal(detalle.getSubtotal())
                        .observaciones(detalle.getObservaciones())
                        .motivoNoRecepcion(detalle.getMotivoNoRecepcion())
                        .build())
                .collect(Collectors.toList());

        return CompraResponseDTO.builder()
                .id(compra.getId())
                .folio(compra.getFolio())
                .fechaCompra(compra.getFechaCompra())
                .fechaRecepcion(compra.getFechaRecepcion())
                .proveedorId(proveedor.getId())
                .proveedorRazonSocial(proveedor.getRazonSocial())
                .proveedorRfc(proveedor.getRfc())
                .proveedorNombreCompleto(nombreCompleto)
                .entregadoPor(compra.getEntregadoPor())
                .tipoDocumento(compra.getTipoDocumento())
                .numeroDocumento(compra.getNumeroDocumento())
                .metodoPago(compra.getMetodoPago())
                .subtotal(compra.getSubtotal())
                .impuesto(compra.getImpuesto())
                .total(compra.getTotal())
                .observaciones(compra.getObservaciones())
                .estado(compra.getEstado())
                .activo(compra.getActivo())
                .fechaRegistro(compra.getFechaRegistro())
                .fechaActualizacion(compra.getFechaActualizacion())
                .detalles(detalles)
                .build();
    }

    private PagoCuentaPorPagarResponseDTO mapPago(PagoCuentaPorPagarModel pago) {
        return PagoCuentaPorPagarResponseDTO.builder()
                .id(pago.getId())
                .cuentaPorPagarId(pago.getCuentaPorPagar().getId())
                .fechaPago(pago.getFechaPago())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .referencia(pago.getReferencia())
                .observaciones(pago.getObservaciones())
                .usuario(pago.getUsuario())
                .fechaRegistro(pago.getFechaRegistro())
                .build();
    }

    private String resolverEstado(Double montoTotal, Double montoPagado) {
        double total = nvl(montoTotal);
        double pagado = nvl(montoPagado);
        if (pagado <= 0) {
            return "PENDIENTE";
        }
        if (pagado >= total) {
            return "PAGADA";
        }
        return "PARCIAL";
    }

    private double nvl(Double valor) {
        return valor == null ? 0.0 : valor;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private String obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "desconocido";
        }
        return authentication.getName();
    }
}
