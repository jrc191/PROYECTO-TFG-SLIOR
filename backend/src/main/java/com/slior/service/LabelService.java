package com.slior.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.slior.model.Stop;
import com.slior.repository.StopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabelService {

    private final StopRepository stopRepository;
    // Usamos una ruta relativa simple que se creará donde se ejecute el JAR
    private static final String OUTPUT_DIR = "generated-labels";

    @org.springframework.scheduling.annotation.Async
    public void generateLabelsAsync(java.util.List<UUID> stopIds) {
        log.info("Starting ASYNC label generation for {} stops", stopIds.size());
        for (UUID stopId : stopIds) {
            try {
                saveLabelPdfToDisk(stopId);
            } catch (Exception e) {
                log.error("Failed to generate label async for stop {}: {}", stopId, e.getMessage());
            }
        }
        log.info("Finished ASYNC label generation");
    }

    public String saveLabelPdfToDisk(UUID stopId) throws Exception {
        log.info("Request to save PDF for Stop ID: {}", stopId);
        
        File directory = new File(OUTPUT_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            log.info("Directory {} created: {}", directory.getAbsolutePath(), created);
        }

        String fileName = "etiqueta-" + stopId + ".pdf";
        File pdfFile = new File(directory, fileName);
        
        // Si ya existe, no lo volvemos a generar para ahorrar recursos
        if (pdfFile.exists()) {
            log.info("PDF already exists at: {}", pdfFile.getAbsolutePath());
            return pdfFile.getAbsolutePath();
        }

        byte[] pdfBytes = generateLabelPdf(stopId);
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            fos.write(pdfBytes);
        }
        
        log.info("PDF SUCCESSFULLY saved to: {}", pdfFile.getAbsolutePath());
        return pdfFile.getAbsolutePath();
    }

    public byte[] generateLabelPdf(UUID stopId) throws Exception {
        try {
            log.info("Starting professional PDF generation for Stop ID: {}", stopId);
            Stop stop = stopRepository.findById(stopId)
                    .orElseThrow(() -> new RuntimeException("Stop not found: " + stopId));

            String uuidString = stop.getId().toString();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // A6 es ideal para etiquetas
            Document document = new Document(PageSize.A6, 10, 10, 10, 10);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Colores
            Color mainTeal = new Color(175, 203, 198); // Aproximado al de la imagen
            Color darkGray = new Color(44, 47, 51);

            // Fuentes
            Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.NORMAL, darkGray);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL, darkGray);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.GRAY);
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.NORMAL, darkGray);
            Font smallDataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, darkGray);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, darkGray);

            // --- HEADER TABLE ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{3, 1});

            // Columna Izquierda: Empresa y Peso
            PdfPCell companyCell = new PdfPCell();
            companyCell.setBorder(Rectangle.NO_BORDER);
            companyCell.addElement(new Paragraph("SLIOR LOGISTICS", companyFont));
            companyCell.addElement(new Paragraph("PESO: VARIABLE / ESTÁNDAR", labelFont));
            headerTable.addCell(companyCell);

            // Columna Derecha: QR Code
            BufferedImage qrImg = generateQrCodeImage(uuidString, 100, 100);
            Image pdfQr = Image.getInstance(imageToByteArray(qrImg));
            pdfQr.scaleToFit(45, 45);
            PdfPCell qrCell = new PdfPCell(pdfQr);
            qrCell.setBorder(Rectangle.BOX); // OpenPDF usa BOX para borde completo
            qrCell.setBorderWidth(0.5f);
            qrCell.setBorderColor(Color.GRAY);
            qrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            qrCell.setPadding(2);
            headerTable.addCell(qrCell);

            document.add(headerTable);

            // Línea teal
            PdfPTable tealLine = new PdfPTable(1);
            tealLine.setWidthPercentage(100);
            PdfPCell lineCell = new PdfPCell();
            lineCell.setBackgroundColor(mainTeal);
            lineCell.setFixedHeight(3f);
            lineCell.setBorder(Rectangle.NO_BORDER);
            tealLine.addCell(lineCell);
            document.add(new Paragraph(" "));
            document.add(tealLine);

            // Título Principal
            Paragraph mainTitle = new Paragraph("CORREO PRIORITARIO", titleFont);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingBefore(5);
            mainTitle.setSpacingAfter(5);
            document.add(mainTitle);

            // --- INFO TABLE ---
            PdfPTable infoTable = new PdfPTable(1);
            infoTable.setWidthPercentage(100);
            
            // Sección Enviar a
            PdfPCell destCell = new PdfPCell();
            destCell.setPadding(10);
            destCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            destCell.setBorderWidth(1f);
            destCell.setBorderColor(Color.GRAY);
            
            destCell.addElement(new Paragraph("ENTREGAR A:", labelFont));
            destCell.addElement(new Paragraph(stop.getDestinatario().toUpperCase(), dataFont));
            destCell.addElement(new Paragraph(stop.getDireccion(), dataFont));
            if (stop.getTelefonoDestinatario() != null) {
                destCell.addElement(new Paragraph("TÉL: " + stop.getTelefonoDestinatario(), smallDataFont));
            }
            
            // Añadir orden de visita en una esquina si existe
            if (stop.getOrdenVisita() != null) {
                Paragraph pVisita = new Paragraph("PARADA: #" + stop.getOrdenVisita(), labelFont);
                pVisita.setAlignment(Element.ALIGN_RIGHT);
                destCell.addElement(pVisita);
            }
            
            infoTable.addCell(destCell);
            document.add(infoTable);

            // --- BARCODE SECTION ---
            document.add(new Paragraph(" "));
            log.info("Generating professional barcode...");
            BufferedImage barcodeImg = generateCode128Image(uuidString, 600, 100);
            Image pdfBarcode = Image.getInstance(imageToByteArray(barcodeImg));
            pdfBarcode.scaleToFit(260, 80);
            pdfBarcode.setAlignment(Element.ALIGN_CENTER);
            document.add(pdfBarcode);

            Paragraph idText = new Paragraph(uuidString, labelFont);
            idText.setAlignment(Element.ALIGN_CENTER);
            idText.setSpacingBefore(-5);
            document.add(idText);

            // --- FOOTER BAR ---
            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setWidthPercentage(100);
            PdfPCell footerCell = new PdfPCell(new Paragraph("SEGUIMIENTO: " + uuidString, footerFont));
            footerCell.setBackgroundColor(mainTeal);
            footerCell.setPadding(6);
            footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerCell.setBorder(Rectangle.NO_BORDER);
            footerTable.addCell(footerCell);
            
            footerTable.setSpacingBefore(15);
            document.add(footerTable);

            document.close();
            log.info("Professional PDF generation completed");
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("ERROR generating professional PDF: {}", e.getMessage(), e);
            throw e;
        }
    }

    private BufferedImage generateQrCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private BufferedImage generateCode128Image(String text, int width, int height) throws Exception {
        Code128Writer barcodeWriter = new Code128Writer();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // El margen ayuda a que el escáner detecte el inicio/fin del código (quiet zone)
        hints.put(EncodeHintType.MARGIN, 2);
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.CODE_128, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private byte[] imageToByteArray(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
