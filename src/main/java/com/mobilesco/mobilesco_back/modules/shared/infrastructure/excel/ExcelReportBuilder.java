/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/shared/infrastructure/excel/ExcelReportBuilder.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ExcelReportBuilder
 * CONTEXTO: Utilidad compartida para reportes Excel con identidad visual Mobilesco.
 * NOTAS: Mantener aqui el diseno base de reportes para que todos salgan consistentes.
 */
package com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelReportBuilder {

    private static final String BRAND_DARK = "324A46";
    private static final String BRAND_ORANGE = "D3803F";
    private static final String BRAND_LIGHT = "EAF0EF";
    private static final String BRAND_SOFT = "F7FAF9";
    private static final int TABLE_HEADER_ROW = 21;
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ExcelReportBuilder() {
    }

    public static byte[] generate(String sheetName, String title, String[] headers, List<Object[]> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(sanitizeSheetName(sheetName));
            Styles styles = createStyles(workbook);
            XSSFDrawing drawing = sheet.createDrawingPatriarch();

            int dashboardLastCol = Math.max(headers.length - 1, 15);
            prepareDashboardColumns(sheet, dashboardLastCol);
            addBrandHeader(workbook, sheet, drawing, styles, title, dashboardLastCol);
            addSummary(sheet, styles, rows);
            addCharts(sheet, drawing, styles, headers, rows);
            addTable(sheet, styles, headers, rows);
            finishLayout(sheet, headers.length);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el reporte Excel: " + title, e);
        }
    }

    private static Styles createStyles(XSSFWorkbook workbook) {
        Styles styles = new Styles();
        styles.title = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 18);
        titleFont.setColor(color(BRAND_DARK));
        styles.title.setFont(titleFont);
        styles.title.setVerticalAlignment(VerticalAlignment.CENTER);

        styles.subtitle = workbook.createCellStyle();
        XSSFFont subtitleFont = workbook.createFont();
        subtitleFont.setFontHeightInPoints((short) 10);
        subtitleFont.setColor(color("667A76"));
        styles.subtitle.setFont(subtitleFont);
        styles.subtitle.setVerticalAlignment(VerticalAlignment.CENTER);

        styles.section = workbook.createCellStyle();
        XSSFFont sectionFont = workbook.createFont();
        sectionFont.setBold(true);
        sectionFont.setFontHeightInPoints((short) 11);
        sectionFont.setColor(color(BRAND_DARK));
        styles.section.setFont(sectionFont);
        styles.section.setFillForegroundColor(color(BRAND_LIGHT));
        styles.section.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.section.setBorderBottom(BorderStyle.THIN);
        styles.section.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

        styles.kpiLabel = workbook.createCellStyle();
        XSSFFont kpiLabelFont = workbook.createFont();
        kpiLabelFont.setBold(true);
        kpiLabelFont.setFontHeightInPoints((short) 9);
        kpiLabelFont.setColor(color("667A76"));
        styles.kpiLabel.setFont(kpiLabelFont);
        styles.kpiLabel.setAlignment(HorizontalAlignment.CENTER);
        styles.kpiLabel.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.kpiLabel.setFillForegroundColor(color(BRAND_SOFT));
        styles.kpiLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        addThinBorder(styles.kpiLabel);

        styles.kpiValue = workbook.createCellStyle();
        XSSFFont kpiValueFont = workbook.createFont();
        kpiValueFont.setBold(true);
        kpiValueFont.setFontHeightInPoints((short) 16);
        kpiValueFont.setColor(color(BRAND_DARK));
        styles.kpiValue.setFont(kpiValueFont);
        styles.kpiValue.setAlignment(HorizontalAlignment.CENTER);
        styles.kpiValue.setVerticalAlignment(VerticalAlignment.CENTER);
        addThinBorder(styles.kpiValue);

        styles.accentKpiValue = workbook.createCellStyle();
        styles.accentKpiValue.cloneStyleFrom(styles.kpiValue);
        XSSFFont accentFont = workbook.createFont();
        accentFont.setBold(true);
        accentFont.setFontHeightInPoints((short) 16);
        accentFont.setColor(color(BRAND_ORANGE));
        styles.accentKpiValue.setFont(accentFont);

        styles.tableHeader = workbook.createCellStyle();
        XSSFFont tableHeaderFont = workbook.createFont();
        tableHeaderFont.setBold(true);
        tableHeaderFont.setColor(IndexedColors.WHITE.getIndex());
        styles.tableHeader.setFont(tableHeaderFont);
        styles.tableHeader.setAlignment(HorizontalAlignment.CENTER);
        styles.tableHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.tableHeader.setFillForegroundColor(color(BRAND_DARK));
        styles.tableHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.tableHeader.setWrapText(true);
        addThinBorder(styles.tableHeader);

        styles.body = workbook.createCellStyle();
        styles.body.setVerticalAlignment(VerticalAlignment.TOP);
        styles.body.setWrapText(true);
        addThinBorder(styles.body);

        styles.bodyAlt = workbook.createCellStyle();
        styles.bodyAlt.cloneStyleFrom(styles.body);
        styles.bodyAlt.setFillForegroundColor(color(BRAND_SOFT));
        styles.bodyAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styles.number = workbook.createCellStyle();
        styles.number.cloneStyleFrom(styles.body);
        styles.number.setAlignment(HorizontalAlignment.RIGHT);

        styles.numberAlt = workbook.createCellStyle();
        styles.numberAlt.cloneStyleFrom(styles.bodyAlt);
        styles.numberAlt.setAlignment(HorizontalAlignment.RIGHT);

        styles.active = workbook.createCellStyle();
        styles.active.cloneStyleFrom(styles.body);
        XSSFFont activeFont = workbook.createFont();
        activeFont.setBold(true);
        activeFont.setColor(color("1F7A6B"));
        styles.active.setFont(activeFont);
        styles.active.setAlignment(HorizontalAlignment.CENTER);

        styles.inactive = workbook.createCellStyle();
        styles.inactive.cloneStyleFrom(styles.body);
        XSSFFont inactiveFont = workbook.createFont();
        inactiveFont.setBold(true);
        inactiveFont.setColor(color(BRAND_ORANGE));
        styles.inactive.setFont(inactiveFont);
        styles.inactive.setAlignment(HorizontalAlignment.CENTER);

        styles.sourceHeader = workbook.createCellStyle();
        XSSFFont sourceHeaderFont = workbook.createFont();
        sourceHeaderFont.setBold(true);
        sourceHeaderFont.setColor(color(BRAND_DARK));
        styles.sourceHeader.setFont(sourceHeaderFont);
        styles.sourceHeader.setFillForegroundColor(color(BRAND_LIGHT));
        styles.sourceHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        addThinBorder(styles.sourceHeader);

        return styles;
    }

    private static void addBrandHeader(
            XSSFWorkbook workbook,
            XSSFSheet sheet,
            XSSFDrawing drawing,
            Styles styles,
            String title,
            int lastCol) {
        sheet.createRow(0).setHeightInPoints(38);
        sheet.createRow(1).setHeightInPoints(22);
        sheet.createRow(2).setHeightInPoints(16);
        sheet.createRow(3).setHeightInPoints(8);

        addLogo(workbook, drawing);

        merge(sheet, 0, 0, 4, lastCol);
        merge(sheet, 1, 1, 4, lastCol);

        Cell titleCell = sheet.getRow(0).createCell(4);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(styles.title);

        Cell subtitleCell = sheet.getRow(1).createCell(4);
        subtitleCell.setCellValue("Generado: " + LocalDateTime.now().format(REPORT_DATE_FORMAT));
        subtitleCell.setCellStyle(styles.subtitle);

        Row accentRow = sheet.getRow(3);
        for (int col = 0; col <= lastCol; col++) {
            Cell cell = accentRow.createCell(col);
            XSSFCellStyle style = (XSSFCellStyle) sheet.getWorkbook().createCellStyle();
            style.setFillForegroundColor(col < 4 ? color(BRAND_ORANGE) : color(BRAND_DARK));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(style);
        }
    }

    private static void addSummary(XSSFSheet sheet, Styles styles, List<Object[]> rows) {
        int activos = countStatus(rows, true);
        int inactivos = countStatus(rows, false);
        int total = rows.size();
        int sinEstado = Math.max(0, total - activos - inactivos);
        double porcentajeActivo = total == 0 ? 0.0 : (activos * 100.0 / total);

        Row section = row(sheet, 5);
        Cell sectionCell = section.createCell(0);
        sectionCell.setCellValue("Resumen");
        sectionCell.setCellStyle(styles.section);
        merge(sheet, 5, 5, 0, 3);

        String[] labels = {"Total", "Activos", "Inactivos", "% activos"};
        Object[] values = {total, activos, inactivos + sinEstado, porcentajeActivo};
        Row labelRow = row(sheet, 6);
        Row valueRow = row(sheet, 7);

        for (int i = 0; i < labels.length; i++) {
            Cell label = labelRow.createCell(i);
            label.setCellValue(labels[i]);
            label.setCellStyle(styles.kpiLabel);

            Cell value = valueRow.createCell(i);
            if (i == 3) {
                value.setCellValue(String.format(Locale.ROOT, "%.1f%%", (Double) values[i]));
            } else {
                value.setCellValue(((Number) values[i]).doubleValue());
            }
            value.setCellStyle(i == 2 ? styles.accentKpiValue : styles.kpiValue);
        }
    }

    private static void addCharts(
            XSSFSheet sheet,
            XSSFDrawing drawing,
            Styles styles,
            String[] headers,
            List<Object[]> rows) {
        int sourceRow = 10;
        int statusLastRow = writeStatusSource(sheet, styles, sourceRow, rows);
        addBarChart(sheet, drawing, "Registros por estado", sourceRow + 1, statusLastRow, 0, 1, 5, 5, 10, 18);

        int categoryCol = findInterestingCategory(headers, rows);
        if (categoryCol >= 0) {
            int categoryLastRow = writeCategorySource(sheet, styles, sourceRow, 2, headers[categoryCol], rows, categoryCol);
            if (categoryLastRow > sourceRow + 1) {
                addBarChart(
                        sheet,
                        drawing,
                        "Distribucion por " + headers[categoryCol],
                        sourceRow + 1,
                        categoryLastRow,
                        2,
                        3,
                        11,
                        5,
                        16,
                        18);
            }
        }
    }

    private static void addTable(XSSFSheet sheet, Styles styles, String[] headers, List<Object[]> rows) {
        Row headerRow = row(sheet, TABLE_HEADER_ROW);
        headerRow.setHeightInPoints(26);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.tableHeader);
        }

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = row(sheet, TABLE_HEADER_ROW + 1 + rowIndex);
            Object[] values = rows.get(rowIndex);
            for (int col = 0; col < headers.length; col++) {
                Object value = col < values.length ? values[col] : "";
                Cell cell = row.createCell(col);
                setValue(cell, value);
                cell.setCellStyle(styleForValue(styles, value, rowIndex));
            }
        }

        int lastDataRow = Math.max(TABLE_HEADER_ROW, TABLE_HEADER_ROW + rows.size());
        sheet.setAutoFilter(new CellRangeAddress(TABLE_HEADER_ROW, lastDataRow, 0, headers.length - 1));
    }

    private static void finishLayout(XSSFSheet sheet, int headerCount) {
        sheet.createFreezePane(0, TABLE_HEADER_ROW + 1);
        sheet.setDisplayGridlines(false);

        for (int i = 0; i < headerCount; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            int minWidth = 11 * 256;
            int maxWidth = 42 * 256;
            sheet.setColumnWidth(i, Math.min(Math.max(width + 512, minWidth), maxWidth));
        }

        sheet.setColumnWidth(0, Math.min(Math.max(sheet.getColumnWidth(0), 10 * 256), 14 * 256));
        sheet.setZoom(90);
    }

    private static int writeStatusSource(XSSFSheet sheet, Styles styles, int startRow, List<Object[]> rows) {
        Row header = row(sheet, startRow);
        Cell statusHeader = header.createCell(0);
        statusHeader.setCellValue("Estado");
        statusHeader.setCellStyle(styles.sourceHeader);
        Cell totalHeader = header.createCell(1);
        totalHeader.setCellValue("Registros");
        totalHeader.setCellStyle(styles.sourceHeader);

        int activos = countStatus(rows, true);
        int inactivos = countStatus(rows, false);
        int sinEstado = Math.max(0, rows.size() - activos - inactivos);

        writeSourceRow(sheet, styles, startRow + 1, 0, "Activos", activos);
        writeSourceRow(sheet, styles, startRow + 2, 0, "Inactivos", inactivos + sinEstado);
        return startRow + 2;
    }

    private static int writeCategorySource(
            XSSFSheet sheet,
            Styles styles,
            int startRow,
            int startCol,
            String headerName,
            List<Object[]> rows,
            int categoryCol) {
        Map<String, Long> counts = rows.stream()
                .map(row -> row.length > categoryCol ? asText(row[categoryCol]) : "")
                .filter(value -> !value.isBlank())
                .collect(Collectors.groupingBy(
                        value -> value,
                        LinkedHashMap::new,
                        Collectors.counting()));

        List<Map.Entry<String, Long>> top = new ArrayList<>(counts.entrySet());
        top.sort(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey()));

        Row header = row(sheet, startRow);
        Cell categoryHeader = header.createCell(startCol);
        categoryHeader.setCellValue(headerName);
        categoryHeader.setCellStyle(styles.sourceHeader);
        Cell totalHeader = header.createCell(startCol + 1);
        totalHeader.setCellValue("Registros");
        totalHeader.setCellStyle(styles.sourceHeader);

        int currentRow = startRow;
        for (Map.Entry<String, Long> entry : top.stream().limit(8).collect(Collectors.toList())) {
            currentRow++;
            writeSourceRow(sheet, styles, currentRow, startCol, entry.getKey(), entry.getValue());
        }

        return currentRow;
    }

    private static void writeSourceRow(XSSFSheet sheet, Styles styles, int rowIndex, int col, String label, Number value) {
        Row row = row(sheet, rowIndex);
        Cell labelCell = row.createCell(col);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.body);
        Cell valueCell = row.createCell(col + 1);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(styles.number);
    }

    private static void addBarChart(
            XSSFSheet sheet,
            XSSFDrawing drawing,
            String title,
            int firstRow,
            int lastRow,
            int categoryCol,
            int valueCol,
            int col1,
            int row1,
            int col2,
            int row2) {
        if (lastRow < firstRow) {
            return;
        }

        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2);
        org.apache.poi.xssf.usermodel.XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
                sheet,
                new CellRangeAddress(firstRow, lastRow, categoryCol, categoryCol));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet,
                new CellRangeAddress(firstRow, lastRow, valueCol, valueCol));

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        data.setBarDirection(BarDirection.COL);
        XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(categories, values);
        series.setTitle("Registros", null);
        chart.plot(data);
    }

    private static int findInterestingCategory(String[] headers, List<Object[]> rows) {
        List<String> preferred = List.of("tipo", "linea", "familia", "modelo", "nivel", "material", "color", "unidad", "ciudad", "ubicacion");
        for (String preference : preferred) {
            for (int col = 0; col < headers.length; col++) {
                String normalized = normalize(headers[col]);
                if (normalized.contains(preference) && hasUsefulCategories(rows, col)) {
                    return col;
                }
            }
        }
        return -1;
    }

    private static boolean hasUsefulCategories(List<Object[]> rows, int col) {
        long uniqueValues = rows.stream()
                .map(row -> row.length > col ? asText(row[col]) : "")
                .filter(value -> !value.isBlank())
                .distinct()
                .limit(2)
                .count();
        return uniqueValues > 1;
    }

    private static int countStatus(List<Object[]> rows, boolean active) {
        return (int) rows.stream()
                .flatMap(Arrays::stream)
                .filter(value -> {
                    String text = normalize(asText(value));
                    return active ? "activo".equals(text) : "inactivo".equals(text);
                })
                .count();
    }

    private static CellStyle styleForValue(Styles styles, Object value, int rowIndex) {
        String text = normalize(asText(value));
        if ("activo".equals(text)) {
            return styles.active;
        }
        if ("inactivo".equals(text)) {
            return styles.inactive;
        }
        if (value instanceof Number) {
            return rowIndex % 2 == 0 ? styles.number : styles.numberAlt;
        }
        return rowIndex % 2 == 0 ? styles.body : styles.bodyAlt;
    }

    private static void setValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool ? "Si" : "No");
        } else {
            cell.setCellValue(asText(value));
        }
    }

    private static String asText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT)
                        .replace("á", "a")
                        .replace("é", "e")
                        .replace("í", "i")
                        .replace("ó", "o")
                        .replace("ú", "u");
    }

    private static String sanitizeSheetName(String sheetName) {
        String safeName = sheetName == null || sheetName.isBlank() ? "Reporte" : sheetName.trim();
        return safeName.replaceAll("[\\\\/?*\\[\\]:]", " ").substring(0, Math.min(safeName.length(), 31));
    }

    private static void addLogo(XSSFWorkbook workbook, XSSFDrawing drawing) {
        int pictureIndex = workbook.addPicture(createLogoPng(), Workbook.PICTURE_TYPE_PNG);
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 0, 4, 3);
        drawing.createPicture(anchor, pictureIndex);
    }

    private static byte[] createLogoPng() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BufferedImage image = new BufferedImage(760, 220, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setColor(new Color(0, 0, 0, 0));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

            Color dark = Color.decode("#" + BRAND_DARK);
            Color orange = Color.decode("#" + BRAND_ORANGE);

            graphics.setColor(orange);
            graphics.fillOval(24, 22, 44, 44);
            graphics.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.drawLine(36, 96, 148, 152);
            graphics.drawLine(42, 178, 148, 178);

            graphics.setColor(dark);
            graphics.drawLine(76, 104, 178, 54);
            graphics.drawLine(178, 54, 178, 176);
            graphics.drawLine(72, 176, 196, 176);

            graphics.setFont(new Font("SansSerif", Font.BOLD, 76));
            graphics.drawString("Mobilesco", 230, 108);
            graphics.setFont(new Font("SansSerif", Font.PLAIN, 31));
            graphics.drawString("CON FIRMEZA", 234, 158);

            graphics.dispose();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el logo para el reporte Excel", e);
        }
    }

    private static void prepareDashboardColumns(Sheet sheet, int lastCol) {
        for (int col = 0; col <= lastCol; col++) {
            sheet.setColumnWidth(col, 16 * 256);
        }
        sheet.setColumnWidth(0, 14 * 256);
        sheet.setColumnWidth(1, 14 * 256);
        sheet.setColumnWidth(2, 14 * 256);
        sheet.setColumnWidth(3, 14 * 256);
    }

    private static Row row(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return row != null ? row : sheet.createRow(rowIndex);
    }

    private static void merge(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        if (lastRow > firstRow || lastCol > firstCol) {
            sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
        }
    }

    private static XSSFColor color(String hex) {
        return new XSSFColor(hexToBytes(hex), null);
    }

    private static byte[] hexToBytes(String hex) {
        return new byte[] {
                (byte) Integer.parseInt(hex.substring(0, 2), 16),
                (byte) Integer.parseInt(hex.substring(2, 4), 16),
                (byte) Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private static void addThinBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    private static class Styles {
        private XSSFCellStyle title;
        private XSSFCellStyle subtitle;
        private XSSFCellStyle section;
        private XSSFCellStyle kpiLabel;
        private XSSFCellStyle kpiValue;
        private XSSFCellStyle accentKpiValue;
        private XSSFCellStyle tableHeader;
        private XSSFCellStyle body;
        private XSSFCellStyle bodyAlt;
        private XSSFCellStyle number;
        private XSSFCellStyle numberAlt;
        private XSSFCellStyle active;
        private XSSFCellStyle inactive;
        private XSSFCellStyle sourceHeader;
    }
}
