package com.hms.discharge.service;

import com.hms.discharge.dto.DischargeMedicationItemDto;
import com.hms.discharge.dto.DischargeSummaryResponse;
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

/**
 * Renders a DischargeSummaryResponse to a PDF. Generated fresh on every request
 * (GET /discharge/{id}/pdf) rather than persisted to disk, since the summary
 * entity itself - not a point-in-time snapshot - is the source of truth here.
 */
@Service
public class DischargePdfService {

    private static final float MARGIN = 50f;
    private static final float LINE_HEIGHT = 15f;
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    public byte[] generate(DischargeSummaryResponse summary) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;

                y = writeLine(stream, FONT_BOLD, 16, y, "Discharge Summary");
                y -= LINE_HEIGHT / 2;
                y = writeLine(stream, FONT_REGULAR, 10, y, "Patient: " + summary.patientName()
                        + " (" + summary.patientId() + ")");
                y = writeLine(stream, FONT_REGULAR, 10, y, "DOB: " + summary.patientDateOfBirth()
                        + "  Gender: " + summary.patientGender());
                y = writeLine(stream, FONT_REGULAR, 10, y, "Admitted: " + summary.admissionDateTime()
                        + "   Discharged: " + summary.dischargeDateTime());
                y = writeLine(stream, FONT_REGULAR, 10, y, "Length of stay: " + summary.lengthOfStayDays() + " day(s)"
                        + "   Discharge type: " + summary.dischargeType());
                y -= LINE_HEIGHT / 2;

                y = writeLine(stream, FONT_BOLD, 11, y, "Diagnosis");
                y = writeLine(stream, FONT_REGULAR, 10, y, "Primary: " + summary.primaryDiagnosisDescription()
                        + " (" + summary.primaryDiagnosisIcd10() + ")");
                if (summary.secondaryDiagnosisDescription() != null) {
                    y = writeLine(stream, FONT_REGULAR, 10, y, "Secondary: " + summary.secondaryDiagnosisDescription()
                            + " (" + summary.secondaryDiagnosisIcd10() + ")");
                }
                y -= LINE_HEIGHT / 2;

                y = writeLine(stream, FONT_BOLD, 11, y, "Hospital Stay Summary");
                y = writeWrapped(stream, FONT_REGULAR, 10, y, summary.summaryOfHospitalStay());
                y -= LINE_HEIGHT / 2;

                if (!summary.significantProcedures().isEmpty()) {
                    y = writeLine(stream, FONT_BOLD, 11, y, "Significant Procedures");
                    for (String procedure : summary.significantProcedures()) {
                        y = writeLine(stream, FONT_REGULAR, 10, y, "- " + procedure);
                    }
                    y -= LINE_HEIGHT / 2;
                }

                if (!summary.complicationsDuringStay().isEmpty()) {
                    y = writeLine(stream, FONT_BOLD, 11, y, "Complications During Stay");
                    for (String complication : summary.complicationsDuringStay()) {
                        y = writeLine(stream, FONT_REGULAR, 10, y, "- " + complication);
                    }
                    y -= LINE_HEIGHT / 2;
                }

                if (!summary.dischargeMedications().isEmpty()) {
                    y = writeLine(stream, FONT_BOLD, 11, y, "Discharge Medications");
                    for (DischargeMedicationItemDto item : summary.dischargeMedications()) {
                        y = writeLine(stream, FONT_REGULAR, 10, y,
                                "- " + item.drugName() + " " + item.dosage() + " " + item.frequency());
                    }
                    y -= LINE_HEIGHT / 2;
                }

                if (summary.dischargeDietPlan() != null) {
                    y = writeLine(stream, FONT_BOLD, 11, y, "Diet Plan");
                    y = writeWrapped(stream, FONT_REGULAR, 10, y, summary.dischargeDietPlan());
                    y -= LINE_HEIGHT / 2;
                }

                y = writeLine(stream, FONT_BOLD, 11, y, "Follow-up");
                y = writeLine(stream, FONT_REGULAR, 10, y, "Date: " + summary.followUpDateTime());
                y = writeWrapped(stream, FONT_REGULAR, 10, y, summary.followUpInstructions());
                y -= LINE_HEIGHT / 2;

                writeLine(stream, FONT_REGULAR, 10, y, "Discharged by: " + summary.dischargedByDoctorName());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate discharge summary PDF", e);
        }
    }

    private float writeLine(PDPageContentStream stream, PDType1Font font, float fontSize, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(MARGIN, y);
        stream.showText(text == null ? "" : text);
        stream.endText();
        return y - LINE_HEIGHT;
    }

    private float writeWrapped(PDPageContentStream stream, PDType1Font font, float fontSize, float y, String text) throws IOException {
        if (text == null || text.isBlank()) {
            return y;
        }
        int maxCharsPerLine = 100;
        for (int start = 0; start < text.length(); start += maxCharsPerLine) {
            String chunk = text.substring(start, Math.min(start + maxCharsPerLine, text.length()));
            y = writeLine(stream, font, fontSize, y, chunk);
        }
        return y;
    }
}
