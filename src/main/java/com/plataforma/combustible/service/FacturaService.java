package com.plataforma.combustible.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.plataforma.combustible.entity.Venta;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.entity.PrecioCombustible;
import com.plataforma.combustible.repository.VentaRepository;
import com.plataforma.combustible.repository.PrecioCombustibleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final VentaRepository ventaRepository;
    private final PrecioCombustibleRepository precioCombustibleRepository;

    private static final BigDecimal IVA_PORCENTAJE = new BigDecimal("0.19");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public byte[] generarFacturaPdf(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con id: " + ventaId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 60, 60);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new FacturaHeaderFooter(venta));
            document.open();

            // ── Colores ───────────────────────────────────────────────
            BaseColor azul       = new BaseColor(0, 84, 166);
            BaseColor grisOscuro = new BaseColor(80, 80, 80);
            BaseColor grisMedio  = new BaseColor(136, 136, 136);
            BaseColor azulClaro  = new BaseColor(245, 248, 255);
            BaseColor verdeClaro = new BaseColor(240, 255, 245);
            BaseColor verdeOsc   = new BaseColor(0, 120, 60);
            BaseColor grisLinea  = new BaseColor(200, 200, 200);
            BaseColor filaColor  = new BaseColor(240, 245, 255);
            BaseColor azulTotal  = new BaseColor(230, 240, 255);

            // ── Fuentes ───────────────────────────────────────────────
            Font fTitulo    = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   azul);
            Font fSubtitulo = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   grisOscuro);
            Font fNormal    = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            Font fNegrita   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   BaseColor.BLACK);
            Font fPequena   = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, grisMedio);
            Font fTotal     = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,   azul);
            Font fBlanco    = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   BaseColor.WHITE);
            Font fIdFact    = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,   azul);
            Font fVerdeN    = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   verdeOsc);

            // ── Encabezado ────────────────────────────────────────────
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60f, 40f});

            PdfPCell cEmpresa = new PdfPCell();
            cEmpresa.setBorder(Rectangle.NO_BORDER);
            cEmpresa.setPaddingBottom(10);
            cEmpresa.addElement(new Paragraph("PLATAFORMA COMBUSTIBLE", fTitulo));
            cEmpresa.addElement(new Paragraph(
                    "Sistema Integral de Gestion de Combustible", fPequena));
            headerTable.addCell(cEmpresa);

            PdfPCell cNumFact = new PdfPCell();
            cNumFact.setBorder(Rectangle.NO_BORDER);
            cNumFact.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cNumFact.setPaddingBottom(10);
            Paragraph pFact = new Paragraph("FACTURA ELECTRONICA", fSubtitulo);
            pFact.setAlignment(Element.ALIGN_RIGHT);
            cNumFact.addElement(pFact);
            Paragraph pId = new Paragraph("N " + String.format("%08d", venta.getId()), fIdFact);
            pId.setAlignment(Element.ALIGN_RIGHT);
            cNumFact.addElement(pId);
            headerTable.addCell(cNumFact);
            document.add(headerTable);

            document.add(new Paragraph(new Chunk(
                    new LineSeparator(1f, 100f, azul, Element.ALIGN_CENTER, -2))));
            document.add(Chunk.NEWLINE);

            // ── Datos vendedor / emisión ──────────────────────────────
            PdfPTable datosTable = new PdfPTable(2);
            datosTable.setWidthPercentage(100);
            datosTable.setWidths(new float[]{50f, 50f});
            datosTable.setSpacingAfter(12);

            // Bloque VENDEDOR
            PdfPCell cVendedor = new PdfPCell();
            cVendedor.setBorder(Rectangle.BOX);
            cVendedor.setBorderColor(grisLinea);
            cVendedor.setBackgroundColor(azulClaro);
            cVendedor.setPadding(10);

            Usuario vendedor = venta.getUsuario();
            cVendedor.addElement(new Paragraph("DATOS DEL VENDEDOR", fSubtitulo));
            cVendedor.addElement(Chunk.NEWLINE);
            // Usuario solo tiene "nombre" (no apellido)
            cVendedor.addElement(parCampo("Nombre:", vendedor.getNombre(), fNormal, fNegrita));
            cVendedor.addElement(parCampo("Correo:", vendedor.getEmail(), fNormal, fNegrita));
            // rol es String directamente
            cVendedor.addElement(parCampo("Rol:", vendedor.getRol() != null
                    ? vendedor.getRol() : "EMPLEADO", fNormal, fNegrita));
            if (vendedor.getTelefono() != null) {
                cVendedor.addElement(parCampo("Telefono:", vendedor.getTelefono(), fNormal, fNegrita));
            }
            if (venta.getEstacion() != null) {
                cVendedor.addElement(parCampo("Estacion:",
                        venta.getEstacion().getNombre(),    fNormal, fNegrita));
                cVendedor.addElement(parCampo("Direccion:",
                        venta.getEstacion().getUbicacion(), fNormal, fNegrita));
            }
            datosTable.addCell(cVendedor);

            // Bloque EMISIÓN
            PdfPCell cEmision = new PdfPCell();
            cEmision.setBorder(Rectangle.BOX);
            cEmision.setBorderColor(grisLinea);
            cEmision.setBackgroundColor(azulClaro);
            cEmision.setPadding(10);

            String fechaStr = venta.getFechaVenta() != null
                    ? venta.getFechaVenta().format(FORMATTER) : "N/A";
            cEmision.addElement(new Paragraph("DATOS DE EMISION", fSubtitulo));
            cEmision.addElement(Chunk.NEWLINE);
            cEmision.addElement(parCampo("Fecha:", fechaStr, fNormal, fNegrita));
            // tipoVehiculo es String en Venta
            if (venta.getTipoVehiculo() != null && !venta.getTipoVehiculo().isBlank()) {
                cEmision.addElement(parCampo("Tipo Vehiculo:",
                        venta.getTipoVehiculo(), fNormal, fNegrita));
            }
            // subsidioAplicado es Boolean
            String subsidioStr = Boolean.TRUE.equals(venta.getSubsidioAplicado()) ? "Si" : "No";
            cEmision.addElement(parCampo("Subsidio aplicado:", subsidioStr, fNormal, fNegrita));

            // Precio regulado: buscamos el precio vigente de esta estacion + combustible
            String normativaStr = "N/A";
            boolean precioRegulado = false;
            if (venta.getEstacion() != null && venta.getCombustible() != null) {
                var precioOpt = precioCombustibleRepository
                        .findTopByEstacionAndCombustibleOrderByFechaDesc(
                                venta.getEstacion(), venta.getCombustible());
                if (precioOpt.isPresent()) {
                    PrecioCombustible pc = precioOpt.get();
                    precioRegulado = Boolean.TRUE.equals(pc.getPrecioRegulado());
                    normativaStr   = pc.getNormativaAplicada() != null
                            ? pc.getNormativaAplicada() : "N/A";
                }
            }
            cEmision.addElement(parCampo("Precio regulado:",
                    precioRegulado ? "Si" : "No", fNormal, fNegrita));
            cEmision.addElement(parCampo("Normativa:", normativaStr, fNormal, fNegrita));
            datosTable.addCell(cEmision);
            document.add(datosTable);

            // ── Tabla de detalle ──────────────────────────────────────
            Paragraph titDetalle = new Paragraph("DETALLE DE LA VENTA", fSubtitulo);
            titDetalle.setSpacingBefore(4);
            titDetalle.setSpacingAfter(6);
            document.add(titDetalle);

            PdfPTable detTable = new PdfPTable(4);
            detTable.setWidthPercentage(100);
            detTable.setWidths(new float[]{35f, 20f, 22f, 23f});
            detTable.setSpacingAfter(12);

            for (String h : new String[]{"Combustible", "Cantidad (L)", "Precio Unitario", "Monto Total"}) {
                PdfPCell hc = new PdfPCell(new Phrase(h, fBlanco));
                hc.setBackgroundColor(azul);
                hc.setHorizontalAlignment(Element.ALIGN_CENTER);
                hc.setPadding(7);
                hc.setBorder(Rectangle.NO_BORDER);
                detTable.addCell(hc);
            }

            // Usar los campos REALES de Venta: cantidad, precioUnitario, montoTotal
            BigDecimal cantidad      = nvl(venta.getCantidad());
            BigDecimal precioUnit    = nvl(venta.getPrecioUnitario());
            BigDecimal montoTotal    = nvl(venta.getMontoTotal());
            String     tipoComb      = venta.getCombustible() != null
                    ? venta.getCombustible().getDescripcion() : "N/A";

            celdaDet(detTable, tipoComb,                   fNormal, filaColor, Element.ALIGN_LEFT);
            celdaDet(detTable, fmt(cantidad) + " L",       fNormal, filaColor, Element.ALIGN_CENTER);
            celdaDet(detTable, "$ " + fmt(precioUnit),     fNormal, filaColor, Element.ALIGN_RIGHT);
            celdaDet(detTable, "$ " + fmt(montoTotal),     fNormal, filaColor, Element.ALIGN_RIGHT);
            document.add(detTable);

            // ── Tabla de totales ──────────────────────────────────────
            // Base gravable e IVA calculados desde montoTotal
            BigDecimal baseGravable = montoTotal.divide(
                    BigDecimal.ONE.add(IVA_PORCENTAJE), 2, RoundingMode.HALF_UP);
            BigDecimal ivaValor     = montoTotal.subtract(baseGravable);

            // Si hay subsidio: recuperamos el precio original para calcular el descuento
            BigDecimal descuentoSubsidio = BigDecimal.ZERO;
            if (Boolean.TRUE.equals(venta.getSubsidioAplicado())
                    && venta.getEstacion() != null && venta.getCombustible() != null) {
                var precioOpt = precioCombustibleRepository
                        .findTopByEstacionAndCombustibleOrderByFechaDesc(
                                venta.getEstacion(), venta.getCombustible());
                if (precioOpt.isPresent() && precioOpt.get().getPrecioOriginal() != null) {
                    BigDecimal precioOriginal = BigDecimal.valueOf(
                            precioOpt.get().getPrecioOriginal());
                    BigDecimal precioReguladoBD = BigDecimal.valueOf(
                            precioOpt.get().getPrecio());
                    BigDecimal diffPorLitro = precioOriginal.subtract(precioReguladoBD);
                    if (diffPorLitro.compareTo(BigDecimal.ZERO) > 0) {
                        descuentoSubsidio = diffPorLitro.multiply(cantidad)
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                }
            }

            BigDecimal totalFinal = montoTotal; // montoTotal ya es el precio final pagado

            PdfPTable totTable = new PdfPTable(2);
            totTable.setWidthPercentage(50);
            totTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totTable.setWidths(new float[]{60f, 40f});

            filaTot(totTable, "Base gravable (sin IVA):", "$ " + fmt(baseGravable),      fNormal, false);
            filaTot(totTable, "IVA (19%):",                "$ " + fmt(ivaValor),          fNormal, false);
            if (descuentoSubsidio.compareTo(BigDecimal.ZERO) > 0) {
                filaTot(totTable, "Descuento subsidio:",   "- $ " + fmt(descuentoSubsidio), fNormal, false);
            }

            // Línea separadora en totales
            PdfPCell sep = new PdfPCell(new Phrase(" "));
            sep.setColspan(2);
            sep.setBorder(Rectangle.TOP);
            sep.setBorderColor(azul);
            sep.setPadding(3);
            totTable.addCell(sep);

            filaTot(totTable, "TOTAL PAGADO:", "$ " + fmt(totalFinal), fTotal, true);
            document.add(totTable);

            // ── Bloque subsidio ───────────────────────────────────────
            if (Boolean.TRUE.equals(venta.getSubsidioAplicado())) {
                document.add(Chunk.NEWLINE);
                PdfPTable subTable = new PdfPTable(1);
                subTable.setWidthPercentage(100);
                PdfPCell subCell = new PdfPCell();
                subCell.setBorder(Rectangle.BOX);
                subCell.setBorderColor(new BaseColor(0, 150, 80));
                subCell.setBackgroundColor(verdeClaro);
                subCell.setPadding(10);
                subCell.addElement(new Paragraph("INFORMACION DE SUBSIDIO", fVerdeN));
                String msgSubsidio = descuentoSubsidio.compareTo(BigDecimal.ZERO) > 0
                        ? "Se aplico un subsidio de $ " + fmt(descuentoSubsidio) +
                          " al precio del combustible conforme a la normativa: " + normativaStr + "."
                        : "Esta venta tiene subsidio aplicado conforme a la normativa vigente.";
                subCell.addElement(new Paragraph(msgSubsidio, fNormal));
                subTable.addCell(subCell);
                document.add(subTable);
            }

            // ── Nota legal ────────────────────────────────────────────
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(new Chunk(
                    new LineSeparator(1f, 100f, azul, Element.ALIGN_CENTER, -2))));
            Paragraph nota = new Paragraph(
                    "Documento generado automaticamente por el Sistema de Plataforma Combustible. " +
                    "Valido para efectos fiscales conforme a la normativa colombiana vigente (DIAN).",
                    fPequena);
            nota.setAlignment(Element.ALIGN_CENTER);
            nota.setSpacingBefore(6);
            document.add(nota);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando factura PDF: " + e.getMessage(), e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Paragraph parCampo(String label, String valor, Font fN, Font fB) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", fN));
        p.add(new Chunk(valor != null ? valor : "N/A", fB));
        p.setSpacingAfter(3);
        return p;
    }

    private void celdaDet(PdfPTable t, String texto, Font f, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", f));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(align);
        c.setPadding(6);
        c.setBorderColor(new BaseColor(200, 200, 200));
        t.addCell(c);
    }

    private void filaTot(PdfPTable t, String label, String valor, Font f, boolean dest) {
        BaseColor bg = dest ? new BaseColor(230, 240, 255) : null;
        PdfPCell lc = new PdfPCell(new Phrase(label, f));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        lc.setPadding(4);
        if (bg != null) lc.setBackgroundColor(bg);
        PdfPCell vc = new PdfPCell(new Phrase(valor, f));
        vc.setBorder(Rectangle.NO_BORDER);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setPadding(4);
        if (bg != null) vc.setBackgroundColor(bg);
        t.addCell(lc);
        t.addCell(vc);
    }

    private BigDecimal nvl(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    private String fmt(BigDecimal v)     { return String.format("%,.2f", v); }

    // ── Header / Footer de página ─────────────────────────────────────────────

    static class FacturaHeaderFooter extends PdfPageEventHelper {
        private final Venta venta;
        private static final DateTimeFormatter FMT =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        FacturaHeaderFooter(Venta venta) { this.venta = venta; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font f = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
            String fecha = venta.getFechaVenta() != null
                    ? venta.getFechaVenta().format(FMT) : "";
            float y  = document.bottom() - 15;
            float cx = (document.left() + document.right()) / 2;
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("Factura N " + String.format("%08d", venta.getId()), f),
                    document.left(), y, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Emitida: " + fecha, f), cx, y, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Pagina " + writer.getPageNumber(), f),
                    document.right(), y, 0);
        }
    }
}