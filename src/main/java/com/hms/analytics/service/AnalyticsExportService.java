package com.hms.analytics.service;

import com.hms.analytics.dto.AlosEntry;
import com.hms.analytics.dto.DiseaseDistributionEntry;
import com.hms.analytics.dto.KpisResponse;
import com.hms.analytics.dto.MortalityEntry;
import com.hms.analytics.dto.NamedCount;
import com.hms.analytics.dto.ProcedureStatEntry;
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
import java.nio.charset.StandardCharsets;

/**
 * "Export to PDF/Excel" from Stage A is implemented as CSV + PDF here - CSV opens
 * natively in Excel and avoids adding a new dependency (Apache POI) solely for
 * this one endpoint; PDF reuses the same PDFBox pattern as Modules 8 and 14.
 */
@Service
public class AnalyticsExportService {

    private static final float MARGIN = 50f;
    private static final float LINE_HEIGHT = 15f;
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    public byte[] generateCsv(KpisResponse kpis) {
        StringBuilder csv = new StringBuilder();

        csv.append("Patient Volume (").append(kpis.patientVolume().granularity()).append(")\n");
        csv.append("Period,New Registrations,IPD Admissions\n");
        kpis.patientVolume().series().forEach(b ->
                csv.append(b.periodLabel()).append(',').append(b.newRegistrations()).append(',').append(b.ipdAdmissions()).append('\n'));
        csv.append('\n');

        csv.append("Top GI Diseases\nICD-10 Code,Description,Count\n");
        for (DiseaseDistributionEntry e : kpis.topGiDiseases()) {
            csv.append(e.icd10Code()).append(',').append(csvSafe(e.description())).append(',').append(e.count()).append('\n');
        }
        csv.append('\n');

        csv.append("OPD/IPD Ratio\nOPD Encounters,IPD Admissions,Ratio\n");
        csv.append(kpis.opdIpdRatio().opdEncounters()).append(',').append(kpis.opdIpdRatio().ipdAdmissions())
                .append(',').append(kpis.opdIpdRatio().ratio()).append('\n');
        csv.append('\n');

        csv.append("ALOS by Diagnosis\nICD-10 Code,Description,Average LOS (days),Discharge Count\n");
        for (AlosEntry e : kpis.alosByDiagnosis()) {
            csv.append(e.icd10Code()).append(',').append(csvSafe(e.description())).append(',')
                    .append(e.averageLengthOfStayDays()).append(',').append(e.dischargeCount()).append('\n');
        }
        csv.append('\n');

        csv.append("Re-admission Rate\n7-Day %,14-Day %,30-Day %,Discharges Considered\n");
        csv.append(kpis.readmissionRate().rate7DayPercent()).append(',').append(kpis.readmissionRate().rate14DayPercent())
                .append(',').append(kpis.readmissionRate().rate30DayPercent()).append(',')
                .append(kpis.readmissionRate().totalDischargesConsidered()).append('\n');
        csv.append('\n');

        csv.append("Procedure Stats\nProcedure Type,Total,Complications,Complication Rate %,Success Rate %\n");
        for (ProcedureStatEntry e : kpis.procedureStats()) {
            csv.append(csvSafe(e.label())).append(',').append(e.totalCount()).append(',').append(e.complicationCount())
                    .append(',').append(e.complicationRatePercent()).append(',').append(e.successRatePercent()).append('\n');
        }
        csv.append('\n');

        csv.append("Mortality by Diagnosis\nICD-10 Code,Description,Total Discharges,Expired,Mortality Rate %\n");
        for (MortalityEntry e : kpis.mortalityByDiagnosis()) {
            csv.append(e.icd10Code()).append(',').append(csvSafe(e.description())).append(',')
                    .append(e.totalDischarges()).append(',').append(e.expiredCount()).append(',')
                    .append(e.mortalityRatePercent()).append('\n');
        }
        csv.append('\n');

        csv.append("Referral Pattern - Top Referring Doctors\nName,Count\n");
        for (NamedCount n : kpis.referralPattern().topReferringDoctors()) {
            csv.append(csvSafe(n.label())).append(',').append(n.count()).append('\n');
        }
        csv.append('\n');

        csv.append("Patient Satisfaction\n");
        csv.append(kpis.patientSatisfaction().available() ? "Available" : "Not available").append(',')
                .append(csvSafe(kpis.patientSatisfaction().message())).append('\n');

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generatePdf(KpisResponse kpis) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;

                y = writeLine(stream, FONT_BOLD, 16, y, "Clinical KPI Dashboard Export");
                y -= LINE_HEIGHT / 2;

                y = writeLine(stream, FONT_BOLD, 11, y, "Top GI Diseases");
                for (DiseaseDistributionEntry e : kpis.topGiDiseases()) {
                    y = writeLine(stream, FONT_REGULAR, 10, y, e.icd10Code() + " - " + e.description() + ": " + e.count());
                }
                y -= LINE_HEIGHT / 2;

                y = writeLine(stream, FONT_BOLD, 11, y, "OPD/IPD Ratio");
                y = writeLine(stream, FONT_REGULAR, 10, y, "OPD: " + kpis.opdIpdRatio().opdEncounters()
                        + "  IPD: " + kpis.opdIpdRatio().ipdAdmissions() + "  Ratio: " + kpis.opdIpdRatio().ratio());
                y -= LINE_HEIGHT / 2;

                y = writeLine(stream, FONT_BOLD, 11, y, "Re-admission Rate");
                y = writeLine(stream, FONT_REGULAR, 10, y, "7-day: " + kpis.readmissionRate().rate7DayPercent()
                        + "%  14-day: " + kpis.readmissionRate().rate14DayPercent()
                        + "%  30-day: " + kpis.readmissionRate().rate30DayPercent() + "%");
                y -= LINE_HEIGHT / 2;

                y = writeLine(stream, FONT_BOLD, 11, y, "Mortality by Diagnosis");
                for (MortalityEntry e : kpis.mortalityByDiagnosis()) {
                    y = writeLine(stream, FONT_REGULAR, 10, y,
                            e.icd10Code() + " - " + e.description() + ": " + e.mortalityRatePercent() + "%");
                }
                y -= LINE_HEIGHT / 2;

                writeLine(stream, FONT_BOLD, 11, y, "Patient Satisfaction: "
                        + (kpis.patientSatisfaction().available() ? "Available" : "Not available"));
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate analytics export PDF", e);
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

    private String csvSafe(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
