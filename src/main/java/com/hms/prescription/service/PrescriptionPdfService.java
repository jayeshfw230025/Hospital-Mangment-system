package com.hms.prescription.service;

import com.hms.prescription.domain.Prescription;
import com.hms.prescription.domain.PrescriptionItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;

@Service
public class PrescriptionPdfService {

    private static final float MARGIN = 50f;
    private static final float LINE_HEIGHT = 16f;
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private final PrescriptionPdfStorageService prescriptionPdfStorageService;

    public PrescriptionPdfService(PrescriptionPdfStorageService prescriptionPdfStorageService) {
        this.prescriptionPdfStorageService = prescriptionPdfStorageService;
    }

    public GeneratedPdf generate(Prescription prescription) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;

                y = writeLine(stream, FONT_BOLD, 16, MARGIN, y, "E-Prescription");
                y -= LINE_HEIGHT / 2;
                y = writeLine(stream, FONT_REGULAR, 11, MARGIN, y, "Patient UPID: " + prescription.getPatientUpid());
                y = writeLine(stream, FONT_REGULAR, 11, MARGIN, y,
                        "Date: " + prescription.getPrescribedDate().format(DateTimeFormatter.ISO_DATE));
                y = writeLine(stream, FONT_REGULAR, 11, MARGIN, y, "Prescribing Doctor: " + prescription.getDoctorName());
                if (prescription.getTemplateUsed() != null) {
                    y = writeLine(stream, FONT_REGULAR, 11, MARGIN, y,
                            "Template: " + prescription.getTemplateUsed().getLabel());
                }
                y -= LINE_HEIGHT;

                y = writeLine(stream, FONT_BOLD, 12, MARGIN, y, "Rx");
                y -= LINE_HEIGHT / 2;

                int index = 1;
                for (PrescriptionItem item : prescription.getItems()) {
                    y = writeLine(stream, FONT_BOLD, 11, MARGIN, y,
                            index + ". " + item.getGenericName() + " - " + item.getDosage());
                    y = writeLine(stream, FONT_REGULAR, 10, MARGIN + 15, y, item.getGeneratedInstructions());
                    if (item.getRefillsAllowed() != null && item.getRefillsAllowed() > 0) {
                        y = writeLine(stream, FONT_REGULAR, 10, MARGIN + 15, y,
                                "Refills allowed: " + item.getRefillsAllowed());
                    }
                    y -= LINE_HEIGHT / 2;
                    index++;
                }

                y -= LINE_HEIGHT;
                writeLine(stream, FONT_REGULAR, 10, MARGIN, y, "Digital Signature: " + prescription.getDigitalSignature());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);

            byte[] pdfBytes = outputStream.toByteArray();
            String fileName = "prescription-" + prescription.getId() + ".pdf";
            String storageKey = prescriptionPdfStorageService.store(pdfBytes, fileName);

            return new GeneratedPdf(pdfBytes, fileName, storageKey);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate prescription PDF", e);
        }
    }

    private float writeLine(PDPageContentStream stream, PDType1Font font, float fontSize,
                             float x, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text == null ? "" : text);
        stream.endText();
        return y - LINE_HEIGHT;
    }

    public record GeneratedPdf(byte[] bytes, String fileName, String storageKey) {
    }
}
