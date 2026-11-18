package com.scamshield.reports.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import com.scamshield.reports.dto.ReportResponse;

@Component
public class ReportPdfGenerator {
    private static final float LEFT = 54;
    private static final float BOTTOM = 54;
    private static final float LINE_HEIGHT = 15;
    private static final int WRAP_AT = 94;

    public byte[] generate(ReportResponse report) {
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Writer writer = new Writer(document);
            writer.line("SCAMSHIELD AI", true, 18);
            writer.line("REPORT & ANALYSIS HISTORY", true, 10);
            writer.blank();
            writer.line(report.title(), true, 15);
            writer.line("Report ID: " + report.id(), false, 9);
            writer.line("Category: " + report.type().label(), false, 9);
            writer.line("Status: " + report.status(), false, 9);
            writer.line("Created: " + report.createdAt(), false, 9);
            if (report.score() != null) {
                writer.blank();
                writer.line(report.scoreLabel() + ": " + report.score() + "/100", true, 11);
            }
            writer.blank();
            writer.section("Overview", List.of(report.summary()));
            for (var section : report.sections()) {
                writer.section(section.title(), section.items());
            }
            writer.blank();
            writer.section("Important note", List.of(disclaimer(report)));
            writer.finish();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate the report PDF.", exception);
        }
    }

    private String disclaimer(ReportResponse report) {
        return switch (report.type()) {
            case SCAM_DETECTION, URL_SCAN ->
                "Automated risk assessments can be incomplete. Review source evidence and use your judgment.";
            case RESUME_ANALYSIS ->
                "Resume feedback is an estimate, not an official ATS score or guarantee of hiring outcomes.";
            case JOB_MATCHING ->
                "A job relevance estimate is not a hiring probability or a job-safety guarantee.";
        };
    }

    private static final class Writer {
        private final PDDocument document;
        private final PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream stream;
        private float y;

        private Writer(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        private void line(String text, boolean isBold, int fontSize) throws IOException {
            for (String line : wrap(ascii(text))) {
                if (y < BOTTOM) newPage();
                stream.beginText();
                stream.setFont(isBold ? bold : regular, fontSize);
                stream.newLineAtOffset(LEFT, y);
                stream.showText(line);
                stream.endText();
                y -= Math.max(LINE_HEIGHT, fontSize + 4);
            }
        }

        private void section(String title, List<String> items) throws IOException {
            line(title, true, 11);
            if (items == null || items.isEmpty()) {
                line("No details were stored for this section.", false, 9);
            } else {
                for (String item : items) {
                    if (item != null && !item.isBlank()) line("- " + item, false, 9);
                }
            }
            blank();
        }

        private void blank() throws IOException {
            y -= 6;
            if (y < BOTTOM) newPage();
        }

        private void finish() throws IOException {
            stream.close();
            stream = null;
        }

        private void newPage() throws IOException {
            if (stream != null) stream.close();
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PDRectangle.LETTER.getHeight() - 54;
        }

        private String ascii(String value) {
            return value == null ? "" : value.replaceAll("[^\\x20-\\x7E]", "?");
        }

        private List<String> wrap(String value) {
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : value.split("\\s+")) {
                if (word.length() > WRAP_AT) {
                    if (!line.isEmpty()) {
                        lines.add(line.toString());
                        line.setLength(0);
                    }
                    for (int start = 0; start < word.length(); start += WRAP_AT) {
                        lines.add(word.substring(start, Math.min(start + WRAP_AT, word.length())));
                    }
                } else if (line.length() + word.length() + (line.isEmpty() ? 0 : 1) > WRAP_AT) {
                    lines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                } else {
                    if (!line.isEmpty()) line.append(' ');
                    line.append(word);
                }
            }
            if (!line.isEmpty()) lines.add(line.toString());
            return lines.isEmpty() ? List.of("") : lines;
        }
    }
}
