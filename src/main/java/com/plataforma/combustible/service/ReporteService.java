package com.plataforma.combustible.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.plataforma.combustible.entity.Venta;
import com.plataforma.combustible.repository.VentaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteService {

    private final VentaRepository ventaRepository;

    public ReporteService(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    // ============================================
    // GENERAR PDF
    // ============================================
    public byte[] generarReporteVentasPDF(Long estacionId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Venta> ventas = ventaRepository.findByEstacionIdAndFechaVentaBetween(estacionId, fechaInicio, fechaFin);
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Título - Usar com.itextpdf.text.Font explícitamente
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            Paragraph title = new Paragraph("REPORTE DE VENTAS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Información del reporte
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            document.add(new Paragraph("Fecha de generación: " + LocalDateTime.now().format(formatter)));
            document.add(new Paragraph("Período: " + fechaInicio.format(formatter) + " - " + fechaFin.format(formatter)));
            document.add(new Paragraph(" "));

            // Tabla de ventas
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            
            // Encabezados
            String[] headers = {"Fecha", "Combustible", "Cantidad (gal)", "Precio Unitario", "Subsidio", "Total"};
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Datos
            double totalGeneral = 0;
            for (Venta venta : ventas) {
                table.addCell(venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                table.addCell(venta.getCombustible().getNombre());
                table.addCell(String.format("%.2f", venta.getCantidad()));
                table.addCell(String.format("$%.2f", venta.getPrecioUnitario()));
                
                boolean tieneSubsidio = venta.getSubsidioAplicado() != null && venta.getSubsidioAplicado();
                table.addCell(tieneSubsidio ? "Sí" : "No");
                
                double monto = venta.getMontoTotal() != null ? venta.getMontoTotal().doubleValue() : 0;
                table.addCell(String.format("$%.2f", monto));
                totalGeneral += monto;
            }

            document.add(table);
            document.add(new Paragraph(" "));
            
            // Total general
            com.itextpdf.text.Font totalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            Paragraph total = new Paragraph("TOTAL GENERAL: $" + String.format("%,.2f", totalGeneral), totalFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    // ============================================
    // GENERAR EXCEL
    // ============================================
    public byte[] generarReporteVentasExcel(Long estacionId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Venta> ventas = ventaRepository.findByEstacionIdAndFechaVentaBetween(estacionId, fechaInicio, fechaFin);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte de Ventas");
            
            // Estilo para encabezados - Usar org.apache.poi.ss.usermodel.Font explícitamente
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Estilo para moneda
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
            
            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE VENTAS DE COMBUSTIBLE");
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Información del reporte
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            Row infoRow1 = sheet.createRow(2);
            infoRow1.createCell(0).setCellValue("Fecha de generación: " + LocalDateTime.now().format(formatter));
            Row infoRow2 = sheet.createRow(3);
            infoRow2.createCell(0).setCellValue("Período: " + fechaInicio.format(formatter) + " - " + fechaFin.format(formatter));
            
            // Encabezados
            String[] headers = {"Fecha", "Combustible", "Cantidad (gal)", "Precio Unitario", "Subsidio", "Total"};
            Row headerRow = sheet.createRow(5);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            
            // Datos
            int rowNum = 6;
            double totalGeneral = 0;
            for (Venta venta : ventas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                row.createCell(1).setCellValue(venta.getCombustible().getNombre());
                row.createCell(2).setCellValue(venta.getCantidad().doubleValue());
                row.createCell(3).setCellValue(venta.getPrecioUnitario().doubleValue());
                row.getCell(3).setCellStyle(currencyStyle);
                
                boolean tieneSubsidio = venta.getSubsidioAplicado() != null && venta.getSubsidioAplicado();
                row.createCell(4).setCellValue(tieneSubsidio ? "Sí" : "No");
                
                double monto = venta.getMontoTotal() != null ? venta.getMontoTotal().doubleValue() : 0;
                row.createCell(5).setCellValue(monto);
                row.getCell(5).setCellStyle(currencyStyle);
                totalGeneral += monto;
            }
            
            // Total general
            Row totalRow = sheet.createRow(rowNum + 1);
            totalRow.createCell(4).setCellValue("TOTAL GENERAL:");
            Cell totalCell = totalRow.createCell(5);
            totalCell.setCellValue(totalGeneral);
            totalCell.setCellStyle(currencyStyle);
            
            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage());
        }
    }
}