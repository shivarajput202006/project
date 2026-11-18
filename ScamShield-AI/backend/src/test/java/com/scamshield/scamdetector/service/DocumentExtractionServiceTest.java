package com.scamshield.scamdetector.service;

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

import com.scamshield.scamdetector.util.DetectedFileType;
import com.scamshield.scamdetector.util.FileValidationUtil;

class DocumentExtractionServiceTest {
    private final DocumentExtractionService extractionService = new DocumentExtractionService();

    @Test
    void extractsTextFromPdf() throws Exception {
        byte[] pdf = createPdf();

        DocumentExtractionService.ExtractedDocument result =
                extractionService.extract(DetectedFileType.PDF, pdf);

        assertTrue(result.text().contains("Application processing fee"));
    }

    @Test
    void extractsTextFromDocx() throws Exception {
        byte[] docx = createDocx();

        DocumentExtractionService.ExtractedDocument result =
                extractionService.extract(DetectedFileType.DOCX, docx);

        assertTrue(result.text().contains("Application processing fee"));
    }

    @Test
    void rejectsMalformedDocumentsAndDocumentsWithoutText() {
        assertThrows(
                FileValidationUtil.InvalidFileException.class,
                () -> extractionService.extract(DetectedFileType.PDF, new byte[] {'%', 'P', 'D', 'F', '-'}));
    }

    private byte[] createPdf() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Application processing fee");
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] createDocx() throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("Application processing fee");
            document.write(output);
            return output.toByteArray();
        }
    }
}
