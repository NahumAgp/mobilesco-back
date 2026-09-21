package com.mobilesco.mobilesco_back.modules.cotizacion.application.usecases;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
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
import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.ImagenService;
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
    private final ImagenService imagenService;

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
        String normalizada = normalizar(busqueda);
        List<String> tokens = normalizada.isBlank() ? List.of() : Arrays.stream(normalizada.split("\\s+"))
                .filter(token -> !token.isBlank()).toList();
        return productoRepository.buscarActivosParaCotizacion().stream()
                .filter(producto -> coincideTodosLosTerminos(producto, tokens, tipo))
                .map(this::evaluarProducto)
                .limit(20)
                .toList();
    }

    @Transactional
    public CotizacionResponseDTO crear(CotizacionRequestDTO dto) {
        ClienteModel cliente = dto.getClienteId() == null ? null : clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        if (cliente != null && !Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ValidationException("El cliente seleccionado está inactivo");
        }
        boolean usaUtilidadPorDetalle = dto.getDetalles().stream().anyMatch(item -> item.getUtilidadPorcentaje() != null);
        if (usaUtilidadPorDetalle && dto.getDetalles().stream().anyMatch(item -> item.getUtilidadPorcentaje() == null)) {
            throw new ValidationException("Todos los productos deben tener utilidad por renglón o usar un margen global");
        }
        validarPorcentajes(dto, usaUtilidadPorDetalle);

        Set<Long> ids = new HashSet<>();
        CotizacionModel cotizacion = CotizacionModel.builder()
                .folio("TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                .cliente(cliente)
                .estado(dto.getEstado() == null ? EstadoCotizacion.PENDIENTE : dto.getEstado())
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(dto.getVigenciaDias()))
                .margenPorcentaje(dto.getMargenPorcentaje() == null ? null : escala(dto.getMargenPorcentaje()))
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
        BigDecimal divisorMargen = dto.getMargenPorcentaje() == null ? null
                : BigDecimal.ONE.subtract(dto.getMargenPorcentaje().divide(CIEN, 8, RoundingMode.HALF_UP));

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
            BigDecimal utilidad = item.getUtilidadPorcentaje();
            BigDecimal precioUnitario = utilidad == null
                    ? costoUnitario.divide(divisorMargen, 2, RoundingMode.HALF_UP)
                    : costoUnitario.multiply(BigDecimal.ONE.add(utilidad.divide(CIEN, 8, RoundingMode.HALF_UP)))
                            .setScale(2, RoundingMode.HALF_UP);
            BigDecimal cantidad = BigDecimal.valueOf(item.getCantidad());
            BigDecimal importe = precioUnitario.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);

            cotizacion.agregarDetalle(CotizacionDetalleModel.builder()
                    .producto(producto)
                    .skuSnapshot(producto.getSku())
                    .nombreSnapshot(producto.getNombre())
                    .cantidad(item.getCantidad())
                    .utilidadPorcentaje(utilidad == null ? null : escala(utilidad))
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
    public CotizacionResponseDTO actualizar(Long id, CotizacionRequestDTO dto) {
        CotizacionModel cotizacion = buscarCotizacion(id);
        if (cotizacion.getEstado() == EstadoCotizacion.COMPLETADA || cotizacion.getEstado() == EstadoCotizacion.CANCELADA) {
            throw new ValidationException("Una cotización completada o cancelada no se puede editar");
        }
        ClienteModel cliente = dto.getClienteId() == null ? null : clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        if (cliente != null && !Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ValidationException("El cliente seleccionado está inactivo");
        }
        boolean usaUtilidadPorDetalle = dto.getDetalles().stream().anyMatch(item -> item.getUtilidadPorcentaje() != null);
        if (usaUtilidadPorDetalle && dto.getDetalles().stream().anyMatch(item -> item.getUtilidadPorcentaje() == null)) {
            throw new ValidationException("Todos los productos deben tener utilidad por renglón o usar un margen global");
        }
        validarPorcentajes(dto, usaUtilidadPorDetalle);
        Set<Long> ids = new HashSet<>();
        BigDecimal subtotalCostos = BigDecimal.ZERO;
        BigDecimal subtotalVenta = BigDecimal.ZERO;
        BigDecimal divisorMargen = dto.getMargenPorcentaje() == null ? null
                : BigDecimal.ONE.subtract(dto.getMargenPorcentaje().divide(CIEN, 8, RoundingMode.HALF_UP));

        cotizacion.setCliente(cliente);
        cotizacion.setEstado(dto.getEstado() == null ? cotizacion.getEstado() : dto.getEstado());
        cotizacion.setFechaVencimiento(cotizacion.getFechaEmision().plusDays(dto.getVigenciaDias()));
        cotizacion.setMargenPorcentaje(dto.getMargenPorcentaje() == null ? null : escala(dto.getMargenPorcentaje()));
        cotizacion.setDescuentoPorcentaje(escala(dto.getDescuentoPorcentaje()));
        cotizacion.setFlete(escala(dto.getFlete()));
        cotizacion.setIvaPorcentaje(escala(dto.getIvaPorcentaje()));
        cotizacion.setNotas(limpiar(dto.getNotas()));
        cotizacion.setCondiciones(limpiar(dto.getCondiciones()));
        cotizacion.getDetalles().clear();

        for (CotizacionRequestDTO.DetalleRequest item : dto.getDetalles()) {
            if (!ids.add(item.getProductoId())) throw new ValidationException("No se puede repetir el mismo producto; ajusta su cantidad");
            ProductoModel producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));
            if (!Boolean.TRUE.equals(producto.getActivo())) throw new ValidationException("El producto " + producto.getSku() + " está inactivo");
            BigDecimal costoUnitario = moneda(costosValidos(producto).getCostoTotal());
            BigDecimal utilidad = item.getUtilidadPorcentaje();
            BigDecimal precioUnitario = utilidad == null
                    ? costoUnitario.divide(divisorMargen, 2, RoundingMode.HALF_UP)
                    : costoUnitario.multiply(BigDecimal.ONE.add(utilidad.divide(CIEN, 8, RoundingMode.HALF_UP))).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cantidad = BigDecimal.valueOf(item.getCantidad());
            BigDecimal importe = precioUnitario.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
            cotizacion.agregarDetalle(CotizacionDetalleModel.builder().producto(producto).skuSnapshot(producto.getSku())
                    .nombreSnapshot(producto.getNombre()).cantidad(item.getCantidad())
                    .utilidadPorcentaje(utilidad == null ? null : escala(utilidad)).costoUnitario(costoUnitario)
                    .precioUnitario(precioUnitario).importe(importe).build());
            subtotalCostos = subtotalCostos.add(costoUnitario.multiply(cantidad));
            subtotalVenta = subtotalVenta.add(importe);
        }
        BigDecimal descuento = subtotalVenta.multiply(dto.getDescuentoPorcentaje()).divide(CIEN, 2, RoundingMode.HALF_UP);
        BigDecimal subtotalConFlete = subtotalVenta.subtract(descuento).add(dto.getFlete()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = subtotalConFlete.multiply(dto.getIvaPorcentaje()).divide(CIEN, 2, RoundingMode.HALF_UP);
        cotizacion.setSubtotalCostos(escala(subtotalCostos));
        cotizacion.setSubtotalVenta(escala(subtotalVenta));
        cotizacion.setMontoDescuento(escala(descuento));
        cotizacion.setSubtotalConFlete(escala(subtotalConFlete));
        cotizacion.setMontoIva(escala(iva));
        cotizacion.setTotal(escala(subtotalConFlete.add(iva)));
        return mapear(cotizacionRepository.save(cotizacion));
    }

    @Transactional
    public CotizacionResponseDTO cambiarEstado(Long id, EstadoCotizacion estado) {
        if (estado == null) throw new ValidationException("El estado es obligatorio");
        CotizacionModel cotizacion = buscarCotizacion(id);
        if (estado == EstadoCotizacion.COMPLETADA && cotizacion.getEstado() != EstadoCotizacion.ACEPTADA
                && cotizacion.getEstado() != EstadoCotizacion.COMPLETADA) {
            throw new ValidationException("La cotización solo se completa al convertirla en una orden de producción");
        }
        if (cotizacion.getEstado() == EstadoCotizacion.COMPLETADA && estado != EstadoCotizacion.COMPLETADA) {
            throw new ValidationException("Una cotización completada no puede cambiar de estatus");
        }
        if (cotizacion.getEstado() == EstadoCotizacion.CANCELADA && estado != EstadoCotizacion.CANCELADA) {
            throw new ValidationException("Una cotización cancelada no puede reabrirse");
        }
        cotizacion.setEstado(estado);
        return mapear(cotizacionRepository.save(cotizacion));
    }

    private ProductoCotizableDTO evaluarProducto(ProductoModel producto) {
        try {
            ProductoEstructuraCostosDTO costos = costosValidos(producto);
            return ProductoCotizableDTO.builder().id(producto.getId()).sku(producto.getSku())
                    .nombre(producto.getNombre()).modelo(producto.getModelo() == null ? null : producto.getModelo().getNombre())
                    .linea(nombreLinea(producto)).familia(producto.getModelo() == null || producto.getModelo().getFamilia() == null
                            ? null : producto.getModelo().getFamilia().getNombre())
                    .imagenPrincipalUrl(imagenPrincipal(producto))
                    .cotizable(true).costoTotal(moneda(costos.getCostoTotal()))
                    .faltantes(List.of()).build();
        } catch (RuntimeException ex) {
            return ProductoCotizableDTO.builder().id(producto.getId()).sku(producto.getSku())
                    .nombre(producto.getNombre()).modelo(producto.getModelo() == null ? null : producto.getModelo().getNombre())
                    .linea(nombreLinea(producto)).familia(producto.getModelo() == null || producto.getModelo().getFamilia() == null
                            ? null : producto.getModelo().getFamilia().getNombre())
                    .imagenPrincipalUrl(imagenPrincipal(producto))
                    .cotizable(false).costoTotal(BigDecimal.ZERO)
                    .faltantes(List.of(ex.getMessage() == null ? "Esquema de costos incompleto" : ex.getMessage())).build();
        }
    }

    private String imagenPrincipal(ProductoModel producto) {
        var imagen = imagenService.obtenerPrincipalPorProducto(producto.getId());
        return imagen == null ? null : imagen.getUrl();
    }

    private boolean coincideTodosLosTerminos(ProductoModel producto, List<String> tokens, String tipo) {
        if ("CODIGO".equalsIgnoreCase(tipo) && !tokens.isEmpty()) {
            String sku = normalizar(producto.getSku());
            return tokens.stream().allMatch(sku::contains);
        }
        String texto = normalizar(String.join(" ",
                producto.getSku(), producto.getNombre(), producto.getDescripcion(), producto.getDescripcionCorta(),
                producto.getModelo() == null ? null : producto.getModelo().getCodigo(),
                producto.getModelo() == null ? null : producto.getModelo().getNombre(),
                producto.getModelo() == null || producto.getModelo().getFamilia() == null ? null : producto.getModelo().getFamilia().getNombre(),
                producto.getModelo() == null || producto.getModelo().getFamilia() == null || producto.getModelo().getFamilia().getLinea() == null
                        ? null : producto.getModelo().getFamilia().getLinea().getNombre(),
                producto.getLinea() == null ? null : producto.getLinea().getNombre()));
        return tokens.stream().allMatch(texto::contains);
    }

    private String nombreLinea(ProductoModel producto) {
        if (producto.getModelo() != null && producto.getModelo().getFamilia() != null
                && producto.getModelo().getFamilia().getLinea() != null) {
            return producto.getModelo().getFamilia().getLinea().getNombre();
        }
        return producto.getLinea() == null ? null : producto.getLinea().getNombre();
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim();
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

    private void validarPorcentajes(CotizacionRequestDTO dto, boolean usaUtilidadPorDetalle) {
        if (!usaUtilidadPorDetalle && (dto.getMargenPorcentaje() == null || dto.getMargenPorcentaje().compareTo(BigDecimal.ZERO) <= 0
                || dto.getMargenPorcentaje().compareTo(new BigDecimal("95")) > 0))
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
                .id(c.getId()).folio(c.getFolio())
                .clienteId(cliente == null ? null : cliente.getId())
                .clienteNombre(cliente == null ? "Público general" : nombreCliente(cliente))
                .clienteWhatsapp(cliente == null ? null : cliente.getWhatsapp())
                .clienteCorreo(cliente == null ? null : cliente.getCorreo()).estado(c.getEstado())
                .fechaEmision(c.getFechaEmision()).fechaVencimiento(c.getFechaVencimiento())
                .margenPorcentaje(c.getMargenPorcentaje()).descuentoPorcentaje(c.getDescuentoPorcentaje())
                .flete(c.getFlete()).ivaPorcentaje(c.getIvaPorcentaje()).subtotalCostos(c.getSubtotalCostos())
                .subtotalVenta(c.getSubtotalVenta()).montoDescuento(c.getMontoDescuento())
                .subtotalConFlete(c.getSubtotalConFlete()).montoIva(c.getMontoIva()).total(c.getTotal())
                .notas(c.getNotas()).condiciones(c.getCondiciones()).fechaRegistro(c.getFechaRegistro())
                .detalles(c.getDetalles().stream().map(d -> CotizacionResponseDTO.Detalle.builder()
                        .id(d.getId()).productoId(d.getProducto().getId()).sku(d.getSkuSnapshot())
                        .nombre(d.getNombreSnapshot()).cantidad(d.getCantidad()).utilidadPorcentaje(d.getUtilidadPorcentaje())
                        .costoUnitario(d.getCostoUnitario())
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
