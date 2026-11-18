package com.scamshield.resume.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import com.scamshield.resume.model.ResumeFileType;
import com.scamshield.resume.util.ResumeException;

@Service
public class ResumeExtractionService {
    private static final int MAX_DOCUMENT_CHARACTERS = 50_000;
    private static final int MAX_PDF_PAGES = 100;
    private static final int MIN_PDF_TEXT_CHARACTERS = 30;
    private static final long MAX_DOCX_ENTRY_BYTES = 10L * 1024 * 1024;
    private static final long MAX_DOCX_TEXT_BYTES = 8L * 1024 * 1024;

    public ExtractedDocument extract(ResumeFileType fileType, byte[] content) {
        return switch (fileType) {
            case PDF -> extractPdf(content);
            case DOCX -> extractDocx(content);
        };
    }

    private ExtractedDocument extractPdf(byte[] content) {
        try (var document = Loader.loadPDF(content)) {
            if (document.isEncrypted()) {
                throw ResumeException.badRequest("Password-protected PDFs cannot be analyzed.");
            }
            if (document.getNumberOfPages() > MAX_PDF_PAGES) {
                throw ResumeException.badRequest("PDFs with more than 100 pages are not supported.");
            }
            String text = new PDFTextStripper().getText(document);
            String normalized = normalize(text);
            if (normalized.isEmpty()) {
                throw ResumeException.badRequest("No readable text was found in the document.");
            }
            long readableCharacters = normalized.codePoints().filter(Character::isLetterOrDigit).count();
            if (readableCharacters < MIN_PDF_TEXT_CHARACTERS) {
                throw ResumeException.badRequest(
                        "This PDF has too little extractable text. It may be scanned; OCR is not configured.");
            }
            return checkedText(normalized);
        } catch (IOException exception) {
            throw ResumeException.badRequest("The PDF is malformed or could not be read.");
        }
    }

    private ExtractedDocument extractDocx(byte[] content) {
        ZipSecureFile.setMaxEntrySize(MAX_DOCX_ENTRY_BYTES);
        ZipSecureFile.setMaxTextSize(MAX_DOCX_TEXT_BYTES);
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(content));
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return checkedText(extractor.getText());
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ResumeException resumeException) {
                throw resumeException;
            }
            throw ResumeException.badRequest("The DOCX is malformed or could not be read.");
        }
    }

    private String normalize(String text) {
        return text == null ? "" : text.replace("\u0000", "").strip();
    }

    private ExtractedDocument checkedText(String text) {
        String normalized = normalize(text);
        if (normalized.isEmpty()) {
            throw ResumeException.badRequest("No readable text was found in the document.");
        }
        boolean truncated = normalized.length() > MAX_DOCUMENT_CHARACTERS;
        if (truncated) {
            normalized = normalized.substring(0, MAX_DOCUMENT_CHARACTERS);
        }
        List<String> limitations = new ArrayList<>();
        if (truncated) {
            limitations.add("Only the first 50,000 extracted document characters were analyzed.");
        }
        return new ExtractedDocument(normalized, limitations);
    }

    public record ExtractedDocument(String text, List<String> limitations) {
        public ExtractedDocument {
            limitations = List.copyOf(limitations);
        }
    }
}
