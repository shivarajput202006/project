package com.scamshield.scamdetector.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class FileValidationUtilTest {
    private final FileValidationUtil validationUtil = new FileValidationUtil();

    @Test
    void acceptsPngBasedOnContentRatherThanBrowserMimeType() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", output);

        assertEquals(DetectedFileType.PNG, validationUtil.validate("offer.png", output.toByteArray()));
    }

    @Test
    void rejectsExtensionWhoseContentDoesNotMatch() {
        assertThrows(
                FileValidationUtil.InvalidFileException.class,
                () -> validationUtil.validate("offer.pdf", new byte[] {0x50, 0x4b, 0x03, 0x04}));
    }

    @Test
    void acceptsDocxOnlyWhenArchiveHasWordDocumentParts() throws IOException {
        byte[] docx = createDocx();

        assertEquals(DetectedFileType.DOCX, validationUtil.validate("offer.docx", docx));
        assertThrows(
                FileValidationUtil.InvalidFileException.class,
                () -> validationUtil.validate("offer.docx", new byte[] {0x50, 0x4b, 0x03, 0x04}));
    }

    private byte[] createDocx() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            addEntry(zip, "[Content_Types].xml");
            addEntry(zip, "word/document.xml");
        }
        return output.toByteArray();
    }

    private void addEntry(ZipOutputStream zip, String name) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write("<document/>".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
