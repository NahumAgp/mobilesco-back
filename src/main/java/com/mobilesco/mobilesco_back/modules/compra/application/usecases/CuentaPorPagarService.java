package com.mobilesco.mobilesco_back.modules.compra.application.usecases;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

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
    public List<CuentaPorPagarResponseDTO> listar(String estado, String busqueda, LocalDate fechaInicio, LocalDate fechaFin) {
        return cuentaPorPagarRepository
                .buscarReporte(normalizarTexto(estado), normalizarTexto(busqueda), fechaInicio, fechaFin)
                .stream()
                .map(cuenta -> mapToResponseDTO(cuenta, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CuentaPorPagarResponseDTO> listarPaginado(
            String estado,
            String busqueda,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Pageable pageable) {
        return cuentaPorPagarRepository
                .buscarPaginado(normalizarTexto(estado), normalizarTexto(busqueda), fechaInicio, fechaFin, pageable)
                .map(cuenta -> mapToResponseDTO(cuenta, false));
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(String estado, String busqueda, LocalDate fechaInicio, LocalDate fechaFin) {
        List<CuentaPorPagarModel> cuentas = cuentaPorPagarRepository.buscarReporte(
                normalizarTexto(estado),
                normalizarTexto(busqueda),
                fechaInicio,
                fechaFin);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Map<String, CellStyle> styles = crearEstilos(workbook);
            Sheet detalle = workbook.createSheet("Detalle");
            Sheet resumen = workbook.createSheet("Resumen mensual");
            XSSFSheet graficas = workbook.createSheet("Graficas");

            escribirDetalle(detalle, cuentas, styles);
            List<ResumenMensual> resumenMensual = construirResumenMensual(cuentas);
            escribirResumenMensual(resumen, resumenMensual, styles);
            escribirGraficas(graficas, resumenMensual, styles);

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el reporte de cuentas por pagar", e);
        }
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

    private Map<String, CellStyle> crearEstilos(Workbook workbook) {
        Map<String, CellStyle> styles = new LinkedHashMap<>();

        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle title = workbook.createCellStyle();
        title.setFont(titleFont);
        title.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        title.setAlignment(HorizontalAlignment.CENTER);
        styles.put("title", title);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle header = workbook.createCellStyle();
        header.setFont(headerFont);
        header.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        header.setBorderBottom(BorderStyle.THIN);
        styles.put("header", header);

        CellStyle money = workbook.createCellStyle();
        money.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        styles.put("money", money);

        CellStyle date = workbook.createCellStyle();
        date.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
        styles.put("date", date);

        return styles;
    }

    private void escribirDetalle(Sheet sheet, List<CuentaPorPagarModel> cuentas, Map<String, CellStyle> styles) {
        Row title = sheet.createRow(0);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("Reporte de cuentas por pagar");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 10));

        String[] headers = {
                "Compra", "Proveedor", "RFC", "Fecha cuenta", "Vencimiento", "Estado",
                "Total", "Pagado", "Saldo pendiente", "Metodo pago compra", "Observaciones"
        };
        Row header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }

        int rowIndex = 3;
        for (CuentaPorPagarModel cuenta : cuentas) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(cuenta.getCompra().getFolio() != null ? cuenta.getCompra().getFolio() : "Compra #" + cuenta.getCompra().getId());
            row.createCell(1).setCellValue(cuenta.getProveedor().getRazonSocial() != null ? cuenta.getProveedor().getRazonSocial() : "-");
            row.createCell(2).setCellValue(cuenta.getProveedor().getRfc() != null ? cuenta.getProveedor().getRfc() : "-");
            setDateCell(row.createCell(3), cuenta.getFechaCuenta(), styles.get("date"));
            setDateCell(row.createCell(4), cuenta.getFechaVencimiento(), styles.get("date"));
            row.createCell(5).setCellValue(cuenta.getEstado());
            setMoneyCell(row.createCell(6), cuenta.getMontoTotal(), styles.get("money"));
            setMoneyCell(row.createCell(7), cuenta.getMontoPagado(), styles.get("money"));
            setMoneyCell(row.createCell(8), cuenta.getSaldoPendiente(), styles.get("money"));
            row.createCell(9).setCellValue(cuenta.getCompra().getMetodoPago() != null ? cuenta.getCompra().getMetodoPago() : "-");
            row.createCell(10).setCellValue(cuenta.getObservaciones() != null ? cuenta.getObservaciones() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private List<ResumenMensual> construirResumenMensual(List<CuentaPorPagarModel> cuentas) {
        Map<YearMonth, ResumenMensual> resumen = new LinkedHashMap<>();
        cuentas.stream()
                .sorted((a, b) -> a.getFechaCuenta().compareTo(b.getFechaCuenta()))
                .forEach(cuenta -> {
                    YearMonth mes = YearMonth.from(cuenta.getFechaCuenta());
                    ResumenMensual item = resumen.computeIfAbsent(mes, ResumenMensual::new);
                    item.total += nvl(cuenta.getMontoTotal());
                    item.pagado += nvl(cuenta.getMontoPagado());
                    item.pendiente += nvl(cuenta.getSaldoPendiente());
                    item.cuentas += 1;
                    if ("PAGADA".equalsIgnoreCase(cuenta.getEstado())) {
                        item.pagadas += 1;
                    } else if (!"CANCELADA".equalsIgnoreCase(cuenta.getEstado())) {
                        item.pendientes += 1;
                    }
                });
        return new ArrayList<>(resumen.values());
    }

    private void escribirResumenMensual(Sheet sheet, List<ResumenMensual> resumenMensual, Map<String, CellStyle> styles) {
        String[] headers = {"Mes", "Total deuda", "Pagado", "Pendiente", "Cuentas", "Pagadas", "Pendientes"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }

        int rowIndex = 1;
        for (ResumenMensual item : resumenMensual) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.mes.format(MONTH_FORMAT));
            setMoneyCell(row.createCell(1), item.total, styles.get("money"));
            setMoneyCell(row.createCell(2), item.pagado, styles.get("money"));
            setMoneyCell(row.createCell(3), item.pendiente, styles.get("money"));
            row.createCell(4).setCellValue(item.cuentas);
            row.createCell(5).setCellValue(item.pagadas);
            row.createCell(6).setCellValue(item.pendientes);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void escribirGraficas(XSSFSheet sheet, List<ResumenMensual> resumenMensual, Map<String, CellStyle> styles) {
        Row title = sheet.createRow(0);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("Graficas de cuentas por pagar");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));

        String[] headers = {"Mes", "Total deuda", "Pagado", "Pendiente", "Cuentas", "Pagadas", "Pendientes"};
        Row header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        int rowIndex = 3;
        for (ResumenMensual item : resumenMensual) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.mes.format(MONTH_FORMAT));
            setMoneyCell(row.createCell(1), item.total, styles.get("money"));
            setMoneyCell(row.createCell(2), item.pagado, styles.get("money"));
            setMoneyCell(row.createCell(3), item.pendiente, styles.get("money"));
            row.createCell(4).setCellValue(item.cuentas);
            row.createCell(5).setCellValue(item.pagadas);
            row.createCell(6).setCellValue(item.pendientes);
        }

        if (resumenMensual.isEmpty()) {
            return;
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 8, 2, 18, 18);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Pagado vs pendiente por mes");
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Mes");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Monto");

        int lastRow = resumenMensual.size() + 2;
        var meses = XDDFDataSourcesFactory.fromStringCellRange(sheet, new org.apache.poi.ss.util.CellRangeAddress(3, lastRow, 0, 0));
        XDDFNumericalDataSource<Double> pagado = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new org.apache.poi.ss.util.CellRangeAddress(3, lastRow, 2, 2));
        XDDFNumericalDataSource<Double> pendiente = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new org.apache.poi.ss.util.CellRangeAddress(3, lastRow, 3, 3));

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        data.setBarDirection(BarDirection.COL);
        XDDFChartData.Series pagadoSerie = data.addSeries(meses, pagado);
        pagadoSerie.setTitle("Pagado", null);
        XDDFChartData.Series pendienteSerie = data.addSeries(meses, pendiente);
        pendienteSerie.setTitle("Pendiente", null);
        chart.plot(data);
    }

    private void setMoneyCell(Cell cell, Double value, CellStyle style) {
        cell.setCellValue(nvl(value));
        cell.setCellStyle(style);
    }

    private void setDateCell(Cell cell, LocalDate value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value);
            cell.setCellStyle(style);
        } else {
            cell.setCellValue("-");
        }
    }

    private static class ResumenMensual {
        private final YearMonth mes;
        private double total;
        private double pagado;
        private double pendiente;
        private int cuentas;
        private int pagadas;
        private int pendientes;

        private ResumenMensual(YearMonth mes) {
            this.mes = mes;
        }
    }
}
