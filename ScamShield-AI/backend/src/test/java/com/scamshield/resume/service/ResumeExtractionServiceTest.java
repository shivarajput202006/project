package com.scamshield.resume.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import com.scamshield.resume.model.ResumeFileType;
import com.scamshield.resume.util.ResumeException;

class ResumeExtractionServiceTest {
    private final ResumeExtractionService service = new ResumeExtractionService();

    @Test
    void extractsPdfText() throws Exception {
        byte[] content = pdf("Ada Lovelace Java developer with experience building software.");

        var result = service.extract(ResumeFileType.PDF, content);

        assertTrue(result.text().contains("Ada Lovelace"));
        assertTrue(result.limitations().isEmpty());
    }

    @Test
    void extractsDocxText() throws Exception {
        byte[] content;
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("Skills: Java, Spring Boot");
            document.write(output);
            content = output.toByteArray();
        }

        assertTrue(service.extract(ResumeFileType.DOCX, content).text().contains("Spring Boot"));
    }

    @Test
    void distinguishesEmptyPdfFromScannedPdfWithoutOcr() throws Exception {
        ResumeException empty = assertThrows(
                ResumeException.class,
                () -> service.extract(ResumeFileType.PDF, pdf("")));
        assertEquals("No readable text was found in the document.", empty.getMessage());

        ResumeException scanned = assertThrows(
                ResumeException.class,
                () -> service.extract(ResumeFileType.PDF, pdf("Scanned image")));
        assertTrue(scanned.getMessage().contains("OCR is not configured"));
    }

    private byte[] pdf(String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            if (!text.isBlank()) {
                try (PDPageContentStream content = new PDPageContentStream(document, document.getPage(0))) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(30, 700);
                    content.showText(text);
                    content.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
