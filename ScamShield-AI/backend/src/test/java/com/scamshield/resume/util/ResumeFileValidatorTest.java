package com.scamshield.resume.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.scamshield.resume.model.ResumeFileType;

class ResumeFileValidatorTest {
    private static final String DOCX_MIME =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final ResumeFileValidator validator = new ResumeFileValidator(5 * 1024 * 1024);

    @Test
    void acceptsPdfWhoseExtensionMimeAndSignatureAgree() {
        byte[] content = "%PDF-1.7\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

        assertEquals(ResumeFileType.PDF, validator.validate("resume.pdf", "application/pdf", content.length, content));
    }

    @Test
    void acceptsWellFormedDocxArchive() throws Exception {
        byte[] content;
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("Education and skills");
            document.write(output);
            content = output.toByteArray();
        }

        assertEquals(ResumeFileType.DOCX, validator.validate("resume.docx", DOCX_MIME, content.length, content));
    }

    @Test
    void rejectsUnsupportedExtensionAndMismatchedMime() {
        byte[] content = "%PDF-1.7\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

        assertThrows(ResumeException.class,
                () -> validator.validate("resume.exe", "application/pdf", content.length, content));
        assertThrows(ResumeException.class,
                () -> validator.validate("resume.pdf", "text/plain", content.length, content));
    }

    @Test
    void rejectsOversizedAndEmptyFiles() {
        ResumeException oversized = assertThrows(
                ResumeException.class,
                () -> validator.validate("resume.pdf", "application/pdf", 6_000_000, new byte[6_000_000]));
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, oversized.getStatus());
        assertThrows(ResumeException.class, () -> validator.validate("resume.pdf", "application/pdf", 0, new byte[0]));
    }

    @Test
    void rejectsMalformedZipEvenWhenNamedAsDocx() {
        byte[] malformed = new byte[] {'P', 'K', 3, 4, 1, 2, 3};
        assertThrows(
                ResumeException.class,
                () -> validator.validate("resume.docx", DOCX_MIME, malformed.length, malformed));
    }
}
