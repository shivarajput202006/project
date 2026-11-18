package com.scamshield.profile.util;

import java.math.BigDecimal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Year;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.http.HttpStatus;

import com.scamshield.profile.dto.EducationRequest;
import com.scamshield.profile.model.GradeType;

public final class ProfileValidationUtil {
    private static final int MAX_PHOTO_BYTES = 5 * 1024 * 1024;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;

    private ProfileValidationUtil() {
    }

    public static String validatePhoto(byte[] content, String declaredContentType) {
        if (content == null || content.length == 0) {
            throw ProfileException.badRequest("Choose a non-empty JPEG, PNG, or WEBP image.");
        }
        if (content.length > MAX_PHOTO_BYTES) {
            throw new ProfileException(HttpStatus.PAYLOAD_TOO_LARGE, "Profile photos must be 5 MB or smaller.");
        }

        String detected = detectImageType(content);
        if (detected == null || !detected.equalsIgnoreCase(declaredContentType) || !isValidImage(content, detected)) {
            throw ProfileException.badRequest("The uploaded file is not a valid JPEG, PNG, or WEBP image.");
        }
        return detected;
    }

    public static void validateEducation(EducationRequest request) {
        int currentYear = Year.now().getValue();
        if (request.startYear() > currentYear + 5) {
            throw ProfileException.badRequest("Start year must be a valid academic year.");
        }
        if (request.endYear() != null && request.endYear() < request.startYear()) {
            throw ProfileException.badRequest("End year cannot be earlier than start year.");
        }
        if (request.endYear() != null && request.endYear() > currentYear) {
            throw ProfileException.badRequest("Completed education cannot end in a future year.");
        }
        if (request.status() == com.scamshield.profile.model.EducationStatus.IN_PROGRESS
                && request.endYear() != null) {
            throw ProfileException.badRequest("In-progress education must not have an end year.");
        }
        if (request.status() == com.scamshield.profile.model.EducationStatus.COMPLETED
                && request.endYear() == null) {
            throw ProfileException.badRequest("Completed education requires an end year.");
        }
        if ((request.gradeType() == null) != (request.gradeValue() == null)) {
            throw ProfileException.badRequest("Provide both a grade type and grade value, or leave both empty.");
        }
        if (request.gradeValue() != null) {
            BigDecimal maximum = request.gradeType() == GradeType.CGPA
                    ? BigDecimal.TEN
                    : BigDecimal.valueOf(100);
            if (request.gradeValue().signum() < 0 || request.gradeValue().compareTo(maximum) > 0) {
                throw ProfileException.badRequest(
                        request.gradeType() == GradeType.CGPA
                                ? "CGPA must be between 0 and 10."
                                : "Percentage must be between 0 and 100.");
            }
        }
    }

    private static String detectImageType(byte[] content) {
        if (content.length >= 3
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (content.length >= 8
                && (content[0] & 0xFF) == 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47
                && content[4] == 0x0D
                && content[5] == 0x0A
                && content[6] == 0x1A
                && content[7] == 0x0A) {
            return "image/png";
        }
        if (content.length >= 12
                && content[0] == 0x52
                && content[1] == 0x49
                && content[2] == 0x46
                && content[3] == 0x46
                && content[8] == 0x57
                && content[9] == 0x45
                && content[10] == 0x42
                && content[11] == 0x50) {
            return "image/webp";
        }
        return null;
    }

    private static boolean isValidImage(byte[] content, String contentType) {
        if ("image/webp".equals(contentType)) {
            long declaredSize = Integer.toUnsignedLong(
                    ByteBuffer.wrap(content, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            if (declaredSize != content.length - 8L || content.length < 20) return false;
            String firstChunk = new String(content, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
            long chunkSize = Integer.toUnsignedLong(
                    ByteBuffer.wrap(content, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            return (firstChunk.equals("VP8 ") || firstChunk.equals("VP8L") || firstChunk.equals("VP8X"))
                    && chunkSize > 0
                    && chunkSize <= content.length - 20L;
        }
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            if (input == null) return false;
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) return false;
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                return width > 0
                        && height > 0
                        && (long) width * height <= MAX_IMAGE_PIXELS
                        && reader.read(0) != null;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            return false;
        }
    }
}
