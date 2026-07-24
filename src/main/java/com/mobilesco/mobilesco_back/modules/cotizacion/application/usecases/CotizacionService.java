package com.mobilesco.mobilesco_back.modules.cotizacion.application.usecases;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.out.persistence.repositories.ClienteRepository;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.*;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos.*;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.application.usecases.ProductoService;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoEstructuraCostosDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CotizacionService {
    private static final BigDecimal CIEN = new BigDecimal("100");
    private final CotizacionRepository cotizacionRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    @Transactional(readOnly = true)
    public Page<CotizacionResponseDTO> listar(EstadoCotizacion estado, String busqueda, Pageable pageable) {
        String texto = busqueda == null || busqueda.isBlank() ? null : busqueda.trim();
        return cotizacionRepository.buscar(estado, texto, pageable).map(this::mapear);
    }

    @Transactional(readOnly = true)
    public CotizacionResponseDTO obtener(Long id) {
        return mapear(buscarCotizacion(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoCotizableDTO> buscarProductos(String busqueda, String tipo) {
        String texto = busqueda == null || busqueda.isBlank() ? null : busqueda.trim();
        Page<ProductoModel> pagina = productoRepository.buscarPaginado(
                true, texto, null, null, null, PageRequest.of(0, 20, Sort.by("nombre").ascending()));
        return pagina.stream()
                .filter(p -> !"CODIGO".equalsIgnoreCase(tipo)
                        || texto == null
                        || p.getSku().toLowerCase(Locale.ROOT).contains(texto.toLowerCase(Locale.ROOT)))
                .map(this::evaluarProducto)
                .toList();
    }

    @Transactional
    public CotizacionResponseDTO crear(CotizacionRequestDTO dto) {
        ClienteModel cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ValidationException("El cliente seleccionado está inactivo");
        }
        validarPorcentajes(dto);

        Set<Long> ids = new HashSet<>();
        CotizacionModel cotizacion = CotizacionModel.builder()
                .folio("TEMP-" + UUID.randomUUID())
                .cliente(cliente)
                .estado(dto.getEstado() == null ? EstadoCotizacion.PENDIENTE : dto.getEstado())
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(dto.getVigenciaDias()))
                .margenPorcentaje(escala(dto.getMargenPorcentaje()))
                .descuentoPorcentaje(escala(dto.getDescuentoPorcentaje()))
                .flete(escala(dto.getFlete()))
                .ivaPorcentaje(escala(dto.getIvaPorcentaje()))
                .notas(limpiar(dto.getNotas()))
                .condiciones(limpiar(dto.getCondiciones()))
                .subtotalCostos(BigDecimal.ZERO)
                .subtotalVenta(BigDecimal.ZERO)
                .montoDescuento(BigDecimal.ZERO)
                .subtotalConFlete(BigDecimal.ZERO)
                .montoIva(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal subtotalCostos = BigDecimal.ZERO;
        BigDecimal subtotalVenta = BigDecimal.ZERO;
        BigDecimal divisorMargen = BigDecimal.ONE.subtract(dto.getMargenPorcentaje().divide(CIEN, 8, RoundingMode.HALF_UP));

        for (CotizacionRequestDTO.DetalleRequest item : dto.getDetalles()) {
            if (!ids.add(item.getProductoId())) {
                throw new ValidationException("No se puede repetir el mismo producto; ajusta su cantidad");
            }
            ProductoModel producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));
            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new ValidationException("El producto " + producto.getSku() + " está inactivo");
            }
            ProductoEstructuraCostosDTO costos = costosValidos(producto);
            BigDecimal costoUnitario = moneda(costos.getCostoTotal());
            BigDecimal precioUnitario = costoUnitario.divide(divisorMargen, 2, RoundingMode.HALF_UP);
            BigDecimal cantidad = BigDecimal.valueOf(item.getCantidad());
            BigDecimal importe = precioUnitario.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);

            cotizacion.agregarDetalle(CotizacionDetalleModel.builder()
                    .producto(producto)
                    .skuSnapshot(producto.getSku())
                    .nombreSnapshot(producto.getNombre())
                    .cantidad(item.getCantidad())
                    .costoUnitario(costoUnitario)
                    .precioUnitario(precioUnitario)
                    .importe(importe)
                    .build());
            subtotalCostos = subtotalCostos.add(costoUnitario.multiply(cantidad));
            subtotalVenta = subtotalVenta.add(importe);
        }

        BigDecimal descuento = subtotalVenta.multiply(dto.getDescuentoPorcentaje())
                .divide(CIEN, 2, RoundingMode.HALF_UP);
        BigDecimal subtotalConFlete = subtotalVenta.subtract(descuento).add(dto.getFlete()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = subtotalConFlete.multiply(dto.getIvaPorcentaje()).divide(CIEN, 2, RoundingMode.HALF_UP);
        cotizacion.setSubtotalCostos(escala(subtotalCostos));
        cotizacion.setSubtotalVenta(escala(subtotalVenta));
        cotizacion.setMontoDescuento(escala(descuento));
        cotizacion.setSubtotalConFlete(escala(subtotalConFlete));
        cotizacion.setMontoIva(escala(iva));
        cotizacion.setTotal(escala(subtotalConFlete.add(iva)));

        cotizacionRepository.saveAndFlush(cotizacion);
        cotizacion.setFolio("COT-" + LocalDate.now().getYear() + "-" + String.format("%05d", cotizacion.getId()));
        return mapear(cotizacionRepository.save(cotizacion));
    }

    @Transactional
    public CotizacionResponseDTO cambiarEstado(Long id, EstadoCotizacion estado) {
        if (estado == null) throw new ValidationException("El estado es obligatorio");
        CotizacionModel cotizacion = buscarCotizacion(id);
        cotizacion.setEstado(estado);
        return mapear(cotizacionRepository.save(cotizacion));
    }

    private ProductoCotizableDTO evaluarProducto(ProductoModel producto) {
        try {
            ProductoEstructuraCostosDTO costos = costosValidos(producto);
            return ProductoCotizableDTO.builder().id(producto.getId()).sku(producto.getSku())
                    .nombre(producto.getNombre()).cotizable(true).costoTotal(moneda(costos.getCostoTotal()))
                    .faltantes(List.of()).build();
        } catch (RuntimeException ex) {
            return ProductoCotizableDTO.builder().id(producto.getId()).sku(producto.getSku())
                    .nombre(producto.getNombre()).cotizable(false).costoTotal(BigDecimal.ZERO)
                    .faltantes(List.of(ex.getMessage() == null ? "Esquema de costos incompleto" : ex.getMessage())).build();
        }
    }

    private ProductoEstructuraCostosDTO costosValidos(ProductoModel producto) {
        ProductoEstructuraCostosDTO c = productoService.obtenerEstructuraCostos(producto.getId());
        List<String> faltantes = new ArrayList<>();
        if (c.getInsumos() == null || c.getInsumos().isEmpty()) faltantes.add("insumos");
        else if (c.getInsumos().stream().anyMatch(i -> nz(i.getCantidad()) <= 0 || nz(i.getCostoUnitario()) <= 0))
            faltantes.add("cantidades o costos de insumos");
        if (c.getOperaciones() == null || c.getOperaciones().isEmpty()) faltantes.add("operaciones");
        else if (c.getOperaciones().stream().anyMatch(o -> o.getCantidad() == null || o.getCantidad() <= 0
                || nz(o.getTiempoTotal()) <= 0 || nz(o.getCostoMinutoOperacion()) <= 0 || nz(o.getImporteActividad()) <= 0))
            faltantes.add("cantidades, tiempos o costos de operaciones");
        if (c.getConfiguracionCifId() == null || nz(c.getTasaCifMinuto()) <= 0 || nz(c.getCostoCif()) <= 0)
            faltantes.add("configuración CIF");
        if (nz(c.getCostoTotal()) <= 0) faltantes.add("costo total");
        if (!faltantes.isEmpty()) {
            throw new ValidationException("No cotizable: falta " + String.join(", ", faltantes));
        }
        return c;
    }

    private void validarPorcentajes(CotizacionRequestDTO dto) {
        if (dto.getMargenPorcentaje() == null || dto.getMargenPorcentaje().compareTo(BigDecimal.ZERO) <= 0
                || dto.getMargenPorcentaje().compareTo(new BigDecimal("95")) > 0)
            throw new ValidationException("El margen debe ser mayor a 0 y máximo 95%");
        if (dto.getDescuentoPorcentaje() == null || dto.getFlete() == null || dto.getIvaPorcentaje() == null)
            throw new ValidationException("Descuento, flete e IVA son obligatorios");
    }

    private CotizacionModel buscarCotizacion(Long id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada"));
    }

    private CotizacionResponseDTO mapear(CotizacionModel c) {
        ClienteModel cliente = c.getCliente();
        return CotizacionResponseDTO.builder()
                .id(c.getId()).folio(c.getFolio()).clienteId(cliente.getId()).clienteNombre(nombreCliente(cliente))
                .clienteWhatsapp(cliente.getWhatsapp()).clienteCorreo(cliente.getCorreo()).estado(c.getEstado())
                .fechaEmision(c.getFechaEmision()).fechaVencimiento(c.getFechaVencimiento())
                .margenPorcentaje(c.getMargenPorcentaje()).descuentoPorcentaje(c.getDescuentoPorcentaje())
                .flete(c.getFlete()).ivaPorcentaje(c.getIvaPorcentaje()).subtotalCostos(c.getSubtotalCostos())
                .subtotalVenta(c.getSubtotalVenta()).montoDescuento(c.getMontoDescuento())
                .subtotalConFlete(c.getSubtotalConFlete()).montoIva(c.getMontoIva()).total(c.getTotal())
                .notas(c.getNotas()).condiciones(c.getCondiciones()).fechaRegistro(c.getFechaRegistro())
                .detalles(c.getDetalles().stream().map(d -> CotizacionResponseDTO.Detalle.builder()
                        .id(d.getId()).productoId(d.getProducto().getId()).sku(d.getSkuSnapshot())
                        .nombre(d.getNombreSnapshot()).cantidad(d.getCantidad()).costoUnitario(d.getCostoUnitario())
                        .precioUnitario(d.getPrecioUnitario()).importe(d.getImporte()).build()).toList())
                .build();
    }

    private String nombreCliente(ClienteModel c) {
        if (c.getNombreComercial() != null && !c.getNombreComercial().isBlank()) return c.getNombreComercial();
        if (c.getRazonSocial() != null && !c.getRazonSocial().isBlank()) return c.getRazonSocial();
        return c.getNombre();
    }
    private String limpiar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private BigDecimal escala(BigDecimal valor) { return valor.setScale(2, RoundingMode.HALF_UP); }
    private BigDecimal moneda(Double valor) { return BigDecimal.valueOf(nz(valor)).setScale(2, RoundingMode.HALF_UP); }
    private double nz(Double valor) { return valor == null ? 0 : valor; }
}
