package com.scamshield.resume.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.scamshield.resume.model.ResumeFileType;

@Component
public class ResumeFileValidator {
    private static final String PDF_MIME = "application/pdf";
    private static final String DOCX_MIME =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final int MAX_ARCHIVE_ENTRIES = 500;
    private static final long MAX_ARCHIVE_EXPANSION_BYTES = 30L * 1024 * 1024;
    private final long maxFileBytes;

    @Autowired
    public ResumeFileValidator(@Value("${app.resume.max-file-size:5MB}") org.springframework.util.unit.DataSize maxSize) {
        this(maxSize.toBytes());
    }

    public ResumeFileValidator(long maxFileBytes) {
        this.maxFileBytes = maxFileBytes;
    }

    public ResumeFileType validate(String filename, String contentType, long declaredSize, byte[] content) {
        if (content == null || content.length == 0) {
            throw ResumeException.badRequest("Choose a non-empty PDF or DOCX resume.");
        }
        if (content.length > maxFileBytes || declaredSize > maxFileBytes) {
            throw new ResumeException(
                    org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE,
                    "The resume exceeds the configured file size limit.");
        }
        if (declaredSize != content.length) {
            throw ResumeException.badRequest("The uploaded file could not be validated.");
        }
        if (filename == null || filename.isBlank() || filename.contains("/")
                || filename.contains("\\") || filename.contains("..")) {
            throw ResumeException.badRequest("The file name is invalid.");
        }

        String extension = extensionOf(filename);
        String mimeType = contentType == null
                ? ""
                : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (extension.equals("pdf") && mimeType.equals(PDF_MIME) && hasPdfSignature(content)) {
            return ResumeFileType.PDF;
        }
        if (extension.equals("docx") && mimeType.equals(DOCX_MIME) && hasZipSignature(content)) {
            validateDocxArchive(content);
            return ResumeFileType.DOCX;
        }
        throw ResumeException.badRequest(
                "The file extension, MIME type, and content must match a supported PDF or DOCX resume.");
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean hasPdfSignature(byte[] content) {
        return content.length >= 5
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F'
                && content[4] == '-';
    }

    private boolean hasZipSignature(byte[] content) {
        return content.length >= 4
                && content[0] == 'P'
                && content[1] == 'K'
                && content[2] == 3
                && content[3] == 4;
    }

    private void validateDocxArchive(byte[] content) {
        boolean hasContentTypes = false;
        boolean hasWordDocument = false;
        int entries = 0;
        long expandedBytes = 0;
        byte[] buffer = new byte[4096];

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (++entries > MAX_ARCHIVE_ENTRIES) {
                    throw ResumeException.badRequest("The DOCX contains too many archive entries.");
                }
                String name = entry.getName().replace('\\', '/');
                if (name.startsWith("/") || Arrays.stream(name.split("/")).anyMatch(".."::equals)) {
                    throw ResumeException.badRequest("The DOCX contains an unsafe archive path.");
                }
                hasContentTypes |= name.equals("[Content_Types].xml");
                hasWordDocument |= name.equals("word/document.xml");
                int read;
                while ((read = zip.read(buffer)) != -1) {
                    expandedBytes += read;
                    if (expandedBytes > MAX_ARCHIVE_EXPANSION_BYTES) {
                        throw ResumeException.badRequest("The DOCX expands beyond the supported size.");
                    }
                }
            }
        } catch (IOException exception) {
            throw ResumeException.badRequest("The DOCX archive is malformed.");
        }
        if (!hasContentTypes || !hasWordDocument) {
            throw ResumeException.badRequest("The file is not a valid DOCX document.");
        }
    }
}
