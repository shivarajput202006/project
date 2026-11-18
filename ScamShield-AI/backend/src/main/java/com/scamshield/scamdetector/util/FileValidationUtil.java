package com.scamshield.scamdetector.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Component;

@Component
public class FileValidationUtil {
    public static final int MAX_FILE_BYTES = 5 * 1024 * 1024;
    private static final long MAX_ARCHIVE_EXPANSION_BYTES = 20L * 1024 * 1024;
    private static final int MAX_ARCHIVE_ENTRIES = 1000;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;
    private static final byte[] PNG_SIGNATURE = {
        (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };

    public DetectedFileType validate(String originalFilename, byte[] content) {
        if (content == null || content.length == 0) {
            throw new InvalidFileException("The selected file is empty.");
        }
        if (content.length > MAX_FILE_BYTES) {
            throw new InvalidFileException("Files must be 5 MB or smaller.");
        }
        if (originalFilename == null
                || originalFilename.isBlank()
                || originalFilename.contains("/")
                || originalFilename.contains("\\")
                || originalFilename.contains("..")) {
            throw new InvalidFileException("The file name is invalid.");
        }

        String extension = extensionOf(originalFilename);
        DetectedFileType detectedType = detectContentType(content);
        if (detectedType == null || !allowedExtension(detectedType, extension)) {
            throw new InvalidFileException("The file content does not match a supported PNG, JPG, JPEG, WEBP, PDF, or DOCX file.");
        }
        if (detectedType == DetectedFileType.DOCX) {
            validateDocxArchive(content);
        }
        if (detectedType == DetectedFileType.PNG || detectedType == DetectedFileType.JPEG) {
            validateRasterImage(content, detectedType);
        }
        return detectedType;
    }

    private DetectedFileType detectContentType(byte[] content) {
        if (startsWith(content, PNG_SIGNATURE)) {
            return DetectedFileType.PNG;
        }
        if (content.length >= 3
                && (content[0] & 0xff) == 0xff
                && (content[1] & 0xff) == 0xd8
                && (content[2] & 0xff) == 0xff) {
            return DetectedFileType.JPEG;
        }
        if (isWebp(content)) {
            return DetectedFileType.WEBP;
        }
        if (content.length >= 8
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F'
                && content[4] == '-') {
            return DetectedFileType.PDF;
        }
        if (content.length >= 4
                && content[0] == 'P'
                && content[1] == 'K'
                && content[2] == 3
                && content[3] == 4) {
            return DetectedFileType.DOCX;
        }
        return null;
    }

    private boolean isWebp(byte[] content) {
        if (content.length < 16
                || content[0] != 'R'
                || content[1] != 'I'
                || content[2] != 'F'
                || content[3] != 'F'
                || content[8] != 'W'
                || content[9] != 'E'
                || content[10] != 'B'
                || content[11] != 'P') {
            return false;
        }
        long declaredLength = Integer.toUnsignedLong(
                (content[4] & 0xff)
                        | ((content[5] & 0xff) << 8)
                        | ((content[6] & 0xff) << 16)
                        | ((content[7] & 0xff) << 24));
        String chunk = new String(content, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
        return declaredLength >= 12
                && declaredLength + 8 <= content.length
                && Set.of("VP8 ", "VP8L", "VP8X").contains(chunk);
    }

    private void validateRasterImage(byte[] content, DetectedFileType type) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            if (input == null) {
                throw new InvalidFileException("The image could not be read.");
            }
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new InvalidFileException("The image is malformed or unsupported.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if ((type == DetectedFileType.PNG && !format.equals("png"))
                        || (type == DetectedFileType.JPEG && !format.equals("jpeg") && !format.equals("jpg"))) {
                    throw new InvalidFileException("The image content does not match its format.");
                }
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels <= 0 || pixels > MAX_IMAGE_PIXELS) {
                    throw new InvalidFileException("Image dimensions exceed the supported limit.");
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new InvalidFileException("The image is malformed or could not be read.");
        }
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
                    throw new InvalidFileException("The DOCX contains too many archive entries.");
                }
                String entryName = entry.getName().replace('\\', '/');
                if (entryName.startsWith("/") || containsParentPathSegment(entryName)) {
                    throw new InvalidFileException("The DOCX contains an unsafe archive path.");
                }
                hasContentTypes |= entryName.equals("[Content_Types].xml");
                hasWordDocument |= entryName.equals("word/document.xml");
                int read;
                while ((read = zip.read(buffer)) != -1) {
                    expandedBytes += read;
                    if (expandedBytes > MAX_ARCHIVE_EXPANSION_BYTES) {
                        throw new InvalidFileException("The DOCX expands beyond the supported size.");
                    }
                }
                zip.closeEntry();
            }
        } catch (IOException exception) {
            throw new InvalidFileException("The DOCX archive is malformed.");
        }
        if (!hasContentTypes || !hasWordDocument) {
            throw new InvalidFileException("The file is not a valid DOCX document.");
        }
    }

    private boolean containsParentPathSegment(String path) {
        for (String segment : path.split("/")) {
            if (segment.equals("..")) {
                return true;
            }
        }
        return false;
    }

    private boolean allowedExtension(DetectedFileType type, String extension) {
        return switch (type) {
            case PNG -> extension.equals("png");
            case JPEG -> extension.equals("jpg") || extension.equals("jpeg");
            case WEBP -> extension.equals("webp");
            case PDF -> extension.equals("pdf");
            case DOCX -> extension.equals("docx");
        };
    }

    private String extensionOf(String filename) {
        int separator = filename.lastIndexOf('.');
        if (separator <= 0 || separator == filename.length() - 1) {
            return "";
        }
        return filename.substring(separator + 1).toLowerCase(Locale.ROOT);
    }

    private boolean startsWith(byte[] input, byte[] prefix) {
        if (input.length < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (input[index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }

    public static class InvalidFileException extends RuntimeException {
        public InvalidFileException(String message) {
            super(message);
        }
    }
}
